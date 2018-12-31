package com.github.fred84.accountingtest.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.fred84.accountingtest.web.TransferRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DifferentAccountsTest {

    private final DifferentAccounts.Validator validator = new DifferentAccounts.Validator();

    @Test
    void nullValues() {
        assertFalse(validator.isValid(
                new TransferRequest(null, null, BigDecimal.ONE, "abc"), null)
        );
    }

    @Test
    void sameValues() {
        assertFalse(validator.isValid(
                new TransferRequest(1L, 1L, BigDecimal.ONE, "abc"), null)
        );
    }

    @Test
    void differentValues() {
        assertTrue(validator.isValid(
                new TransferRequest(1L, 2L, BigDecimal.ONE, "abc"), null)
        );
    }
}