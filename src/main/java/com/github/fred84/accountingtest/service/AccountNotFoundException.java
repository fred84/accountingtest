package com.github.fred84.accountingtest.service;

public class AccountNotFoundException extends RuntimeException {

    AccountNotFoundException(long id) {
        super("Account with id " + id + " not found");
    }
}
