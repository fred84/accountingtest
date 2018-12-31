package com.github.fred84.accountingtest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class AccountTest {

    private final Account account = new Account(1L);

    @Test
    void add() {
        account.add(BigDecimal.ONE);
        account.add(BigDecimal.ONE);

        assertThat(account.getBalance(), equalTo(BigDecimal.valueOf(2)));
    }

    @Test
    void subtract_notEnoughBalance() {
        assertFalse(account.subtract(BigDecimal.ONE));
    }

    @Test
    void addAndSubtract() {
        account.add(BigDecimal.TEN);

        assertTrue(account.subtract(BigDecimal.valueOf(9)));

        assertThat(account.getBalance(), equalTo(BigDecimal.ONE));

        assertTrue(account.subtract(BigDecimal.ONE));

        assertThat(account.getBalance(), equalTo(BigDecimal.ZERO));
    }

    @Test
    void add_nonPositiveValue() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> account.add(BigDecimal.ZERO));

        assertThat(e.getMessage(), equalTo("amount must be positive, but 0 given"));
    }

    @Test
    void concurrentAdd() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10000);
        Executor executor = Executors.newFixedThreadPool(100);

        Runnable task = () -> {
            account.add(BigDecimal.ONE);
            latch.countDown();
        };

        for (int i = 0; i < 10000; i++) {
            executor.execute(task);
        }

        latch.await();

        assertThat(account.getBalance(), equalTo(BigDecimal.valueOf(10000)));
    }
}
