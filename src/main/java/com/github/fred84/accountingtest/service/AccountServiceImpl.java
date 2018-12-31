package com.github.fred84.accountingtest.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Singleton;
import lombok.NonNull;

@Singleton
public class AccountServiceImpl implements AccountService {

    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();
    private final Map<Account, Queue<BalanceChange>> balanceChanges = new ConcurrentHashMap<>();

    @Override
    public Account create() {
        Account account = new Account(idGenerator.incrementAndGet());
        accounts.put(account.getId(), account);
        balanceChanges.put(account, new ConcurrentLinkedQueue<>());
        return account;
    }

    @Override
    public Account findById(long id) {
        return accounts.get(id);
    }

    @Override
    public Account getById(long id) {
        return accounts.computeIfAbsent(
                id,
                key -> {
                    throw new AccountNotFoundException(key);
                }
        );
    }

    @Override
    public boolean transfer(
            @NonNull Account from,
            @NonNull Account to,
            @NonNull BigDecimal amount,
            @NonNull String description
    ) {
        if (!from.subtract(amount)) {
            return false;
        }

        to.add(amount);

        balanceChanges.get(from).add(BalanceChange.transfer(from, to, amount.negate(), description));
        balanceChanges.get(to).add(BalanceChange.transfer(to, from, amount, description));

        return true;
    }

    @Override
    public boolean adjustment(
            @NonNull Account account,
            @NonNull BigDecimal amount,
            @NonNull String description
    ) {
        if (!applyAdjustment(account, amount)) {
            return false;
        }

        balanceChanges.get(account).add(BalanceChange.adjustment(account, amount, description));

        return true;
    }

    @Override
    public List<BalanceChange> getBalanceChanges(@NonNull Account account) {
        return List.copyOf(balanceChanges.get(account));
    }

    private boolean applyAdjustment(Account account, BigDecimal amount) {
        if (amount.signum() > 0) {
            return account.add(amount);
        } else {
            return account.subtract(amount.negate());
        }
    }
}
