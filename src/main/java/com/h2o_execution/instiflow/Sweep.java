package com.h2o_execution.instiflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Sweep {
    private String symbol;
    private double averagePrice;
    private double strike;
    private double cashAmount;
    private Trade.OptionType optionType;
    private String expiration;
    private long execTime;
    private ExecutionType type;

    public enum ExecutionType {INTER_MARKET, SINGLE_MARKET}

}
