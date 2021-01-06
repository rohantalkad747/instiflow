package com.h2o_execution.instiflow;

public interface IOrderFlowEventsDB {
    void addBlock(Trade trade);

    void addSweep(Sweep sweep);
}
