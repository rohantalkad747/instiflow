package com.h2o_execution.instiflow;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TradeProcessor {
    private final IOrderFlowEventsReceiver receiver;
    private final ITradeAggregator aggregator;
    private final AtomicReference<Set<SweepSignature>> sweepSignatures = new AtomicReference<>(Sets.newHashSet());

    @Data
    @AllArgsConstructor
    private static class SweepSignature {
        private String symbol;
        private double strike;
    }

    private boolean modifySet(Set<SweepSignature> current, Set<SweepSignature> modified) {
        if (sweepSignatures.compareAndSet(current, modified)) {
            return true;
        }
        return false;
    }

    public void onOrder(Trade trade) throws InterruptedException {
        if (trade.isBlockTrade()) {
            receiver.newBlockTrade(trade);
        } else {
            if (!canCheckForSweep(trade)) {
                checkForSweep(trade);
                stopCheckingForSweep(trade);
            }
        }
    }

    private boolean canCheckForSweep(Trade trade) {
        for (;;) {
            SweepSignature sweepSig = new SweepSignature(trade.getSymbol(), trade.getStrike());
            Set<SweepSignature> current = sweepSignatures.get();
            if (current.contains(sweepSig)) {
                return false;
            }
            Set<SweepSignature> modified = Sets.newHashSet(current.iterator());
            modified.add(sweepSig);
            if (modifySet(current, modified)) {
                return true;
            }
        }
    }

    private void stopCheckingForSweep(Trade trade) {
       for (;;) {
           SweepSignature sweepSig = new SweepSignature(trade.getSymbol(), trade.getStrike());
           Set<SweepSignature> current = sweepSignatures.get();
           Set<SweepSignature> modified = Sets.newHashSet(current.iterator());
           modified.remove(sweepSig);
           if (modifySet(current, modified)) {
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

    private Sweep.Type getSweepType(List<Trade> trades) {
        Set<String> exchanges = trades.stream().map(Trade::getExchange).collect(Collectors.toSet());
        if (exchanges.size() == 1) {
            return Sweep.Type.SINGLE_MARKET;
        }
        return Sweep.Type.INTER_MARKET;
    }

    private void checkForSweep(Trade trade) throws InterruptedException {
        long sweepStartTs = trade.getExecTime();
        // We do this to find all similar orders
        Thread.sleep(1);
        long sweepEndTs = sweepStartTs + 1000;
        List<Trade> trades = aggregator.getTrades(trade.getSymbol(), trade.getStrike(), sweepStartTs, sweepEndTs);
        double cashAmount = calcCashAmount(trades);
        if (qualifiesAsSweep(trades, cashAmount)) {
            processSweep(trade, trades, cashAmount);
        }
    }

    private void processSweep(Trade trade, List<Trade> trades, double cashAmount) {
        double avgPrice = calcAvgPrice(trades);
        Sweep sweep = buildSweep(trade, cashAmount, avgPrice);
        receiver.newSweep(sweep);
    }

    private boolean qualifiesAsSweep(List<Trade> trades, double cashAmount) {
        return trades.size() > 1 && cashAmount >= Trade.Size.SMALL.amount;
    }

    private Sweep buildSweep(Trade trade, double cashAmount, double avgPrice) {
        return new Sweep.SweepBuilder()
                .averagePrice(avgPrice)
                .cashAmount(cashAmount)
                .strike(trade.getStrike())
                .symbol(trade.getSymbol())
                .build();
    }
}
