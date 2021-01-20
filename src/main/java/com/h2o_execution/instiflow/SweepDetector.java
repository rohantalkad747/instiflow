package com.h2o_execution.instiflow;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class SweepDetector {
    private final OrderFlowEventsDB receiver;
    private final TradeDB tradeDB;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Set<SweepSignature> sweepSignatures = Sets.newConcurrentHashSet();

    public SweepDetector(OrderFlowEventsDB receiver, TradeDB tradeDB) {
        this.receiver = receiver;
        this.tradeDB = tradeDB;
        checkForSweeps();
    }

    public void checkForSweep(Trade trade)  {
        SweepSignature sweepSignature = new SweepSignature(trade);
        sweepSignatures.add(sweepSignature);
    }

    private double calcAvgPrice(List<Trade> trades) {
        double priceSum = 0;
        for (Trade trade : trades) {
            priceSum += trade.getPrice();
        }
        return BigDecimal.valueOf(priceSum / trades.size()).doubleValue();
    }

    private double calcCashAmount(List<Trade> trades) {
        double amt = 0;
        for (Trade trade : trades) {
            amt += trade.getPrice() * trade.getQuantity();
        }
        return amt;
    }

    private Sweep.ExecutionType getSweepType(List<Trade> trades) {
        Set<String> exchanges = trades.stream().map(Trade::getExchange).collect(Collectors.toSet());
        if (exchanges.size() == 1) {
            return Sweep.ExecutionType.SINGLE_MARKET;
        }
        return Sweep.ExecutionType.INTER_MARKET;
    }

    private void checkForSweeps() {
        Runnable task = () -> {
            Iterator<SweepSignature> iterator = sweepSignatures.iterator();
            sweepSignatures.clear();
            SweepSignature sweepSignature;
            while (iterator.hasNext()) {
                sweepSignature = iterator.next();
                long sweepEndTs = sweepSignature.startTime + 10;
                List<Trade> trades = tradeDB.getTrades(sweepSignature.getSymbol(), sweepSignature.getExpiration(), sweepSignature.getOptionType(), sweepSignature.getStrike(), sweepSignature.startTime, sweepEndTs);
                double cashAmount = calcCashAmount(trades);
                if (qualifiesAsSweep(trades, cashAmount)) {
                    processSweep(sweepSignature, trades, cashAmount);
                }
            }
        };
        executor.scheduleAtFixedRate(task, 10, 10, TimeUnit.MILLISECONDS);
    }

    private void processSweep(SweepSignature sweepSignature, List<Trade> trades, double cashAmount) {
        double avgPrice = calcAvgPrice(trades);
        Sweep.ExecutionType executionType = getSweepType(trades);
        Sweep sweep = buildSweep(sweepSignature, executionType, cashAmount, sweepSignature.getExpiration(), avgPrice);
        receiver.newSweep(sweep);
    }

    private boolean qualifiesAsSweep(List<Trade> trades, double cashAmount) {
        return childrenExist(trades) && meetsCashAmountThreshold(cashAmount);
    }

    private boolean meetsCashAmountThreshold(double cashAmount) {
        return cashAmount >= Trade.Size.SMALL.amount;
    }

    private boolean childrenExist(List<Trade> trades) {
        return trades.size() > 1;
    }

    private Sweep buildSweep(SweepSignature baseTrade, Sweep.ExecutionType executionType, double cashAmount, String expiration, double avgPrice) {
        return new Sweep.SweepBuilder()
                .averagePrice(avgPrice)
                .cashAmount(cashAmount)
                .type(executionType)
                .optionType(baseTrade.getOptionType())
                .strike(baseTrade.getStrike().doubleValue())
                .expiration(expiration)
                .execTime(baseTrade.getStartTime())
                .symbol(baseTrade.getSymbol())
                .build();
    }

    @Data
    @NoArgsConstructor
    private static class SweepSignature {
        @EqualsAndHashCode.Exclude
        private long startTime;
        private String symbol;
        private Trade.OptionType optionType;
        private BigDecimal strike;
        private String expiration;

        public SweepSignature(Trade trade) {
            this.startTime = trade.getExecTime();
            this.symbol = trade.getSymbol();
            this.optionType = trade.getOptionType();
            this.strike = BigDecimal.valueOf(trade.getStrike());
            this.expiration = trade.getExpiration();
        }
    }
}
