package com.h2o_execution.instiflow;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TradeProcessor {
    private final TradeDB tradeDB;
    private final TradeClassifier tradeClassifier;

    public void onTrade(Trade trade) {
        tradeDB.addTrade(trade);
        tradeClassifier.classify(trade);
    }
}
