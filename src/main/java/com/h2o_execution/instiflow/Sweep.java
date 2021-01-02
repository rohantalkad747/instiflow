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
    private long execTime;
    private Type type;

    public enum Type {INTER_MARKET, SINGLE_MARKET}

}
