package com.github.fred84.accountingtest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class AccountServiceImplTest {

    private final AccountServiceImpl accountRepository = new AccountServiceImpl();

    @Test
    void getById_notExists() {
        assertThrows(AccountNotFoundException.class, () -> accountRepository.getById(1));
    }

    @Test
    void findById_notExists() {
        assertNull(accountRepository.findById(1));
    }

    @Test
    void findById_found() {
        Account account = accountRepository.create();

        assertThat(
                accountRepository.findById(account.getId()),
                equalTo(account)
        );
    }

    @Test
    void succeededAdjustment() {
        Account account = accountRepository.create();

        assertTrue(accountRepository.adjustment(account, BigDecimal.ONE,"initial"));
        assertThat(account.getBalance(), equalTo(BigDecimal.ONE));
    }

    @Test
    void negativeAdjustment() {
        Account account = accountRepository.create();

        accountRepository.adjustment(account, BigDecimal.ONE,"initial");
        accountRepository.adjustment(account, BigDecimal.valueOf(-1),"negative");

        assertThat(account.getBalance(), equalTo(BigDecimal.ZERO));
    }

    @Test
    void rejectedAdjustment() {
        Account account = accountRepository.create();

        assertFalse(accountRepository.adjustment(account, BigDecimal.valueOf(-1),"initial"));
        assertThat(account.getBalance(), equalTo(BigDecimal.ZERO));
    }

    @Test
    void succeededTransfer() {
        Account from = accountRepository.create();
        Account to = accountRepository.create();
        accountRepository.adjustment(from, BigDecimal.ONE,"initial");

        assertTrue(accountRepository.transfer(from, to, BigDecimal.ONE, "descr"));
        assertThat("from balance", from.getBalance(), equalTo(BigDecimal.ZERO));
        assertThat("to balance", to.getBalance(), equalTo(BigDecimal.ONE));
    }

    @Test
    void rejectedTransfer() {
        Account from = accountRepository.create();
        Account to = accountRepository.create();
        accountRepository.adjustment(from, BigDecimal.ONE, "initial");

        assertFalse(accountRepository.transfer(from, to, BigDecimal.TEN, "descr"));
        assertThat("from balance", from.getBalance(), equalTo(BigDecimal.ONE));
        assertThat("to balance", to.getBalance(), equalTo(BigDecimal.ZERO));
    }

    @Test
    void history() {
        Account from = accountRepository.create();
        Account to = accountRepository.create();
        accountRepository.adjustment(from, BigDecimal.ONE, "abc");
        accountRepository.transfer(from, to, BigDecimal.ONE, "def");

        List<BalanceChange> fromHistory = accountRepository.getBalanceChanges(from);
        List<BalanceChange> toHistory = accountRepository.getBalanceChanges(to);

        assertThat("from history", fromHistory, hasSize(2));
        assertThat("to history", toHistory, hasSize(1));

        assertThat(fromHistory, hasItem(hasProperty("amount", equalTo(BigDecimal.valueOf(-1)))));
        assertThat(fromHistory, hasItem(hasProperty("amount", equalTo(BigDecimal.valueOf(1)))));
    }

    @Test
    void concurrentTransfers() throws InterruptedException {
        Account from = accountRepository.create();
        Account to = accountRepository.create();
        accountRepository.adjustment(from, BigDecimal.valueOf(10000),"initial");
        accountRepository.adjustment(to, BigDecimal.valueOf(10000),"initial");

        CountDownLatch latch = new CountDownLatch(10000);
        Executor executor = Executors.newFixedThreadPool(100);

        Runnable transferTask = () -> {
            accountRepository.transfer(from, to, BigDecimal.ONE, "descr");
            latch.countDown();
        };

        Runnable backwardTransferTask = () -> {
            accountRepository.transfer(to, from, BigDecimal.ONE, "descr");
            latch.countDown();
        };

        for (int i = 0; i < 5000; i++) {
            executor.execute(transferTask);
            executor.execute(backwardTransferTask);
        }

        latch.await();

        assertThat("from balance", from.getBalance(), equalTo(BigDecimal.valueOf(10000)));
        assertThat("to balance", to.getBalance(), equalTo(BigDecimal.valueOf(10000)));
        assertThat(
                "from history size",
                accountRepository.getBalanceChanges(from),
                hasSize(equalTo(10000 + 1))
        );
        assertThat(
                "to history size",
                accountRepository.getBalanceChanges(from),
                hasSize(equalTo(10000 + 1))
        );
    }
}