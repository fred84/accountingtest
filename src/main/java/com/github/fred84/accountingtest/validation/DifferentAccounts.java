package com.github.fred84.accountingtest.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import com.github.fred84.accountingtest.web.TransferRequest;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER, TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DifferentAccounts.Validator.class)
public @interface DifferentAccounts {

    class Validator implements ConstraintValidator<DifferentAccounts, TransferRequest> {

        @Override
        public boolean isValid(TransferRequest request, ConstraintValidatorContext context) {
            if (request.getToId() == null || request.getFromId() == null) {
                return false;
            }

            return !request.getToId().equals(request.getFromId());
        }
    }

    String message() default "'To' and 'From' account ids must be different";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
