package com.github.fred84.accountingtest.web;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

@Value
class AdjustmentRequest {
    @Min(1)
    private final Long accountId;
    @DecimalMin(value = "0", inclusive = false)
    @DecimalMax(value = "1000000000")
    private final BigDecimal amount;
    @Length(min = 1, max = 100)
    private final String description;
}
