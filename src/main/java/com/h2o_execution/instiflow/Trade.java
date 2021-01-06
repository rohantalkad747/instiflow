package com.h2o_execution.instiflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
    private final OptionType optionType;

    public enum OptionType {
        CALL, PUT;
    }

    public boolean isBlockTrade() {
        return quantity * price >= Trade.Size.MEDIUM.amount;
    }

    public enum Size {
        SMALL(50_000),
        MEDIUM(200_000),
        LARGE(500_000),
        EXTRA_LARGE(1_000_000);

        public final int amount;

        Size(int amount) {
            this.amount = amount;
        }
    }
}
