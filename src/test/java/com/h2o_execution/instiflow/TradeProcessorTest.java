package com.h2o_execution.instiflow;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

public class TradeProcessorTest {

    TradeDB tradeDB = new TradeDB();
    OrderFlowEventsReceiver orderFlowEventsReceiver = new OrderFlowEventsReceiver();
    TradeClassifier tradeClassifier = new TradeClassifier(orderFlowEventsReceiver, tradeDB);
    TradeProcessor tradeProcessor = new TradeProcessor(tradeDB, tradeClassifier);

    @Test
    void testOnTrade() throws InterruptedException {
        for (int i = 1; i < 8; i++) {
            Trade blockTrade = Trade.builder()
                    .exchange("CHIX" + i)
                    .execTime(new Date().getTime())
                    .price(12.35)
                    .optionType(Trade.OptionType.CALL)
                    .expiration("01-08-21")
                    .quantity(500)
                    .strike(BigDecimal.valueOf(125.50))
                    .symbol("AAPL")
                    .build();
            tradeProcessor.onTrade(blockTrade);
        }
        Thread.sleep(1000);
    }
}