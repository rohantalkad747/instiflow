package com.h2o_execution.instiflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class Sweep {
    private String symbol;
    private List<Trade> trades;
    private double averagePrice;
    private double strike;
    private double cashAmount;

    public enum Type { INTER_MARKET, SINGLE_MARKET };
}
