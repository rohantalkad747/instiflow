package com.h2o_execution.instiflow;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TradeDB {
    private static final int CALL_INDEX = 0;
    private static final int PUT_INDEX = 1;
    private final Map<String, Map<String, List<Map<BigDecimal, List<Trade>>>>> trades = Maps.newConcurrentMap();

    public void addTrade(Trade trade) {
        trades
                .computeIfAbsent(trade.getSymbol(), k -> Maps.newConcurrentMap())
                .computeIfAbsent(trade.getExpiration(), k -> Lists.newArrayList(Maps.newConcurrentMap(), Maps.newConcurrentMap()))
                .get(trade.getOptionType() == Trade.OptionType.CALL ? CALL_INDEX : PUT_INDEX)
                .computeIfAbsent(BigDecimal.valueOf(trade.getStrike()), k -> Lists.newArrayList())
                .add(trade);
    }

    public List<Trade> getTrades(String symbol, String expirationDate, Trade.OptionType optionType, BigDecimal strike, long startTs, long endTs) {
        return trades
                .get(symbol)
                .get(expirationDate)
                .get(optionType == Trade.OptionType.CALL ? CALL_INDEX : PUT_INDEX)
                .get(strike)
                .stream()
                .filter(trade -> trade.getExecTime() >= startTs && trade.getExecTime() <= endTs)
                .collect(Collectors.toList());
    }
}
