package com.h2o_execution.instiflow;

import lombok.Data;

import java.math.BigDecimal;

public class NBBOHistory {

    @Data
    public static class NBBO {
        private BigDecimal bid;
        private BigDecimal offer;
    }

    public NBBO getNBBO(String symbol, long timeStamp, Trade.OptionType type, BigDecimal strike, String exp) {
        return null;
    }
}
