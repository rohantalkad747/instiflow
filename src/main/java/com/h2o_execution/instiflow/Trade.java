package com.h2o_execution.instiflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class Trade {
    private final String exchange;
    private final long execTime;
    private final String symbol;
    private final String expiration;
    private final double price;
    private final BigDecimal strike;
    private final int quantity;
    private final Side executionSide;

    public boolean isBlockTrade() {
        return quantity * price >= Trade.Size.MEDIUM.amount;
    }

    public enum Side {BUY, SELL}

    public enum Size {
        EXTRA_SMALL(10_000),
        SMALL(50_000),
        MEDIUM(100_000),
        LARGE(250_000),
        EXTRA_LARGE(1_000_000);

        public final int amount;

        Size(int amount) {
            this.amount = amount;
        }
    }
}
