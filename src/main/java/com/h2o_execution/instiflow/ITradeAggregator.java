package com.h2o_execution.instiflow;

import java.util.List;

public interface ITradeAggregator {
    List<Trade> getTrades(String symbol, double strike, long startTs, long endTs);

    void onTrade();
}
