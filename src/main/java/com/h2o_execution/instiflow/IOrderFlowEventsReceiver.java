package com.h2o_execution.instiflow;

public interface IOrderFlowEventsReceiver {
    void newBlockTrade(Trade trade);

    void newSweep(Sweep sweep);
}
