package com.github.fred84.accountingtest.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Value;

@Value
public class BalanceChange {

    enum Type {
        ADJUSTMENT,
        TRANSFER
    }

    static BalanceChange transfer(Account account, Account other, BigDecimal amount, String description) {
        return new BalanceChange(account.getId(), amount, Type.TRANSFER, LocalDateTime.now(), description, other.getId());
    }

    static BalanceChange adjustment(Account account, BigDecimal amount, String description) {
        return new BalanceChange(account.getId(), amount, Type.ADJUSTMENT, LocalDateTime.now(), description, 0);
    }

    private final long accountId;
    private final BigDecimal amount;
    private final Type type;
    private final LocalDateTime createdAt;
    private final String description;
    private final long otherId;
}
