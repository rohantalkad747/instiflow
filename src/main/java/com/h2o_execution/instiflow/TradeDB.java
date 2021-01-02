package com.h2o_execution.instiflow;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TradeDB {
    private final int BUY_INDEX = 0;
    private final int SELL_INDEX = 0;
    private final Map<String, Map<String, List<Map<BigDecimal, List<Trade>>>>> trades = Maps.newConcurrentMap();

    public void addTrade(Trade trade) {
        trades
                .computeIfAbsent(trade.getSymbol(), k -> Maps.newConcurrentMap())
                .computeIfAbsent(trade.getExpiration(), k -> Lists.newArrayList(Maps.newConcurrentMap(), Maps.newConcurrentMap()))
                .get(trade.getExecutionSide() == Trade.Side.BUY ? BUY_INDEX : SELL_INDEX)
                .computeIfAbsent(trade.getStrike(), k -> Lists.newArrayList())
                .add(trade);
    }

    public List<Trade> getTrades(String symbol, String expirationDate, Trade.Side side, BigDecimal strike, long startTs, long endTs) {
        return trades
                .getOrDefault(symbol, Maps.newHashMap())
                .getOrDefault(expirationDate, Lists.newArrayList(Maps.newConcurrentMap(), Maps.newConcurrentMap()))
                .get(side == Trade.Side.BUY ? BUY_INDEX : SELL_INDEX)
                .getOrDefault(strike, Lists.newArrayList())
                .stream()
                .filter((Trade trade) -> trade.getExecTime() >= startTs && trade.getExecTime() < endTs)
                .collect(Collectors.toList());
    }
}
