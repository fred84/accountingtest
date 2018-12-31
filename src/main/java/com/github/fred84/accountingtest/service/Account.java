package com.github.fred84.accountingtest.service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(exclude = "balance")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Account {

    private static final AtomicReferenceFieldUpdater<Account, BigDecimal> UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(Account.class, BigDecimal.class, "balance");

    private final long id;
    private volatile BigDecimal balance = BigDecimal.ZERO;

    boolean add(@NonNull BigDecimal value) {
        return applyChange(value, () -> balance.add(value));
    }

    boolean subtract(@NonNull BigDecimal value) {
        return applyChange(value, () -> balance.subtract(value));
    }

    private boolean applyChange(BigDecimal value, Supplier<BigDecimal> operator) {
        if (value.signum() != 1) {
            throw new IllegalArgumentException(String.format("amount must be positive, but %s given", value));
        }

        while (true) {
            BigDecimal current = balance;
            BigDecimal newValue = operator.get();

            if (newValue.signum() < 0) {
                return false;
            }

            if (UPDATER.compareAndSet(this, current, newValue)) {
                return true;
            }
        }
    }
}
