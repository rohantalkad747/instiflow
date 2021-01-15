package com.h2o_execution.instiflow;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class OrderFlowEventsDB {
    private final List<Trade> blocks = Lists.newArrayList();
    private final List<Sweep> sweeps = Lists.newArrayList();

    public void newBlockTrade(Trade trade) {
        log.info("BLOCK TRADE DETECTED: " + trade.toString());
        blocks.add(trade);
    }

    public void newSweep(Sweep sweep) {
        log.info("SWEEP DETECTED: " + sweep.toString());
        sweeps.add(sweep);
    }
}
