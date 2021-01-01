package com.h2o_execution.instiflow;

import lombok.Data;

@Data
public class Trade {

    public enum Side { BUY, SELL }

    private final String exchange;
    private final long execTime;
    private final String symbol;
    private final double price;
    private final double strike;
    private final int quantity;
    private final Side side;

    public enum Size {
        SMALL(50_000),
        MEDIUM(250_000),
        LARGE(1_000_000),
        EXTRA_LARGE(10_000_000);

        public final int amount;

        Size(int amount) {
            this.amount = amount;
        }
    }

    public boolean isBlockTrade() {
        return quantity * price >= Trade.Size.MEDIUM.amount;
    }
}
