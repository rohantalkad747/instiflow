package com.h2o_execution.instiflow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderFlowEventsReceiver {

    void newBlockTrade(Trade trade) {
        log.info("BLOCK TRADE DETECTED: " + trade.toString());
    }

    void newSweep(Sweep sweep) {
        log.info("SWEEP DETECTED: " + sweep.toString());
    }
}
