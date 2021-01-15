package com.h2o_execution.instiflow;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TradeProcessorTest {

    TradeDB tradeDB;
    OrderFlowEventsDB orderFlowEventsDB;
    TradeClassifier tradeClassifier;
    TradeProcessor tradeProcessor;

    @BeforeEach
    void on() {
        tradeDB = new TradeDB();
        orderFlowEventsDB = new OrderFlowEventsDB();
        tradeClassifier = new TradeClassifier(orderFlowEventsDB, tradeDB);
        tradeProcessor = new TradeProcessor(tradeDB, tradeClassifier);
    }

    @Test
    void whenMultipleTradesAreExecutedOnDiffExchangesInLessThanAMillisecond_thenDetectIntermarketSweep()  {
        simulateIntermarketSweep();
        await()
                .atMost(Duration.ofSeconds(1))
                .until(() -> !orderFlowEventsDB.getSweeps().isEmpty());
        List<Sweep> sweeps = orderFlowEventsDB.getSweeps();
        Sweep sweep = sweeps.get(0);
        Sweep expectedSweep = Sweep.builder()
                .symbol("AAPL")
                .averagePrice(12.349999999999996)
                .strike(125.5)
                .cashAmount(67925)
                .optionType(Trade.OptionType.CALL)
                .expiration("01-08-21")
                .execTime(sweep.getExecTime())
                .type(Sweep.ExecutionType.INTER_MARKET)
                .build();
        assertThat(sweep, equalTo(expectedSweep));
    }

    @Test
    void whenTradeExecutedOver200K_thenDetectBlockTrade()  {
        simulateIntermarketSweep();
        await()
                .atMost(Duration.ofSeconds(1))
                .until(() -> !orderFlowEventsDB.getSweeps().isEmpty());
        List<Sweep> sweeps = orderFlowEventsDB.getSweeps();
        Sweep sweep = sweeps.get(0);
        Sweep expectedSweep = Sweep.builder()
                .symbol("AAPL")
                .averagePrice(12.349999999999996)
                .strike(125.5)
                .cashAmount(67925)
                .optionType(Trade.OptionType.CALL)
                .expiration("01-08-21")
                .execTime(sweep.getExecTime())
                .type(Sweep.ExecutionType.INTER_MARKET)
                .build();
        assertThat(sweep, equalTo(expectedSweep));
    }

    private void simulateIntermarketSweep() {
        long execTime = new Date().getTime();
        // Simulate 15 orders in a millisecond
        for (int i = 0; i < 15; i++) {
            Trade childTrade = Trade.builder()
                    .exchange("CHIX" + i)
                    .execTime(execTime + (i * 100))
                    .price(12.35)
                    .optionType(Trade.OptionType.CALL)
                    .expiration("01-08-21")
                    .quantity(500)
                    .strike(125.50)
                    .symbol("AAPL")
                    .build();
            tradeProcessor.onTrade(childTrade);
        }
    }
}