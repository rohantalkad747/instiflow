package com.h2o_execution.instiflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class Sweep {
    private String symbol;
    private double averagePrice;
    private BigDecimal strike;
    private double cashAmount;
    private Trade.OptionType optionType;
    public String expiration;
    private long execTime;
    private ExecutionType type;

    public enum ExecutionType {INTER_MARKET, SINGLE_MARKET}

}
