package com.github.fred84.accountingtest.service;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    /**
     * Create new id.
     */
    Account create();

    /**
     * Find account by id or return null.
     */
    Account findById(long id);

    /**
     * Find account by id or throw exception.
     */
    Account getById(long id);

    /**
     * Transfer given amount from one account to another.
     */
    boolean transfer(Account from, Account to, BigDecimal amount, String description);

    /**
     * Change account balance for given amount (both positive and negative).
     */
    boolean adjustment(Account account, BigDecimal amount, String description);

    /**
     * History of all changes for given account balance. Changes are eventually consistent and maybe out of order.
     */
    List<BalanceChange> getBalanceChanges(Account account);
}
