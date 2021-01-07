package com.h2o_execution.instiflow;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class TradeClassifier {
    private final OrderFlowEventsReceiver receiver;
    private final TradeDB tradeDB;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<Set<SweepSignature>> sweepSignatures = new AtomicReference<>(Sets.newHashSet());

    private boolean modifySet(Set<SweepSignature> current, Set<SweepSignature> modified) {
        return sweepSignatures.compareAndSet(current, modified);
    }

    public void onTrade(Trade trade)  {
        if (trade.isBlockTrade()) {
            receiver.newBlockTrade(trade);
        } else {
            if (canCheckForSweep(trade)) {
                checkForSweep(trade);
            }
        }
    }

    private boolean canCheckForSweep(Trade trade) {
        for (;;) {
            SweepSignature sweepSig = new SweepSignature(trade.getSymbol(), trade.getStrike(), trade.getExpiration());
            Set<SweepSignature> current = sweepSignatures.get();
            if (current.contains(sweepSig)) {
                return false;
            }
            Set<SweepSignature> modified = Sets.newHashSet(current.iterator());
            modified.add(sweepSig);
            if (modifySet(current, modified)) {
                log.info("Sweeping for " + trade);
                return true;
            }
        }
    }

    private void stopCheckingForSweep(Trade trade) {
        for (; ; ) {
            SweepSignature sweepSig = new SweepSignature(trade.getSymbol(), trade.getStrike(), trade.getExpiration());
            Set<SweepSignature> current = sweepSignatures.get();
            Set<SweepSignature> modified = Sets.newHashSet(current.iterator());
            modified.remove(sweepSig);
            if (modifySet(current, modified)) {
                log.info("Done sweeping for " + trade);
                return;
            }
        }
    }

    private double calcAvgPrice(List<Trade> trades) {
        double priceSum = 0;
        for (Trade trade : trades) {
            priceSum += trade.getPrice();
        }
        return priceSum / trades.size();
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

    private void checkForSweep(Trade trade) {
        long sweepStartTs = trade.getExecTime();
        // We do this to find all similar orders
        Runnable task = () -> {
            long sweepEndTs = sweepStartTs + 1000;
            List<Trade> trades = tradeDB.getTrades(trade.getSymbol(), trade.getExpiration(), trade.getOptionType(), trade.getStrike(), sweepStartTs, sweepEndTs);
            double cashAmount = calcCashAmount(trades);
            if (qualifiesAsSweep(trades, cashAmount)) {
                processSweep(trade, trades, cashAmount);
            }
            stopCheckingForSweep(trade);
        };
        executor.schedule(task, 1, TimeUnit.MILLISECONDS);
    }

    private void processSweep(Trade baseTrade, List<Trade> trades, double cashAmount) {
        double avgPrice = calcAvgPrice(trades);
        Sweep.ExecutionType executionType = getSweepType(trades);
        Sweep sweep = buildSweep(baseTrade, executionType, cashAmount, baseTrade.getExpiration(), avgPrice);
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

    private Sweep buildSweep(Trade baseTrade, Sweep.ExecutionType executionType, double cashAmount, String expiration, double avgPrice) {
        return new Sweep.SweepBuilder()
                .averagePrice(avgPrice)
                .cashAmount(cashAmount)
                .type(executionType)
                .optionType(baseTrade.getOptionType())
                .strike(baseTrade.getStrike())
                .expiration(expiration)
                .execTime(baseTrade.getExecTime())
                .symbol(baseTrade.getSymbol())
                .build();
    }

    @Data
    @AllArgsConstructor
    private static class SweepSignature {
        private String symbol;
        private BigDecimal strike;
        private String expiration;
    }
}
