package com.h2o_execution.instiflow;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

public class NBBOHistory {

    @Data
    @AllArgsConstructor
    public static class NBBO {
        private BigDecimal bid;
        private BigDecimal offer;
    }

    public NBBO getNBBO(String symbol, Long timeStamp, Trade.OptionType type, Double strike, String exp) {
        return null;
    }
}
