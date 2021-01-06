package com.h2o_execution.instiflow;

import lombok.Data;

import java.util.List;

@Data
public class OrderFlowEventsDBImpl implements IOrderFlowEventsDB {

    private List<Trade> blocks;
    private List<Sweep> sweeps;

    @Override
    public void addBlock(Trade trade) {
        blocks.add(trade);
    }

    @Override
    public void addSweep(Sweep sweep) {
        sweeps.add(sweep);
    }
}
