package com.h2o_execution.instiflow;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TradeProcessor {
    private final OrderFlowEventsDB receiver;
    private final TradeDB tradeDB;
    private final SweepDetector sweepDetector;

    public void onTrade(Trade trade) {
        if (trade.isBlockTrade()) {
            receiver.newBlockTrade(trade);
        } else {
            tradeDB.addTrade(trade);
            sweepDetector.checkForSweep(trade);
        }
    }
}
