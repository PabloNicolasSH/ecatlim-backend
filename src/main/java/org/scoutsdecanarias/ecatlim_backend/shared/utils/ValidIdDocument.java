package org.scoutsdecanarias.ecatlim_backend.shared.utils;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidIdDocument.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIdDocument {
    String message() default IdDocuments.FORMAT_MESSAGE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidIdDocument, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.isBlank()) {
                return true;
            }
            IdDocuments.Result result = IdDocuments.check(value);
            if (result == IdDocuments.Result.WRONG_CHECK_LETTER) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(IdDocuments.CHECK_LETTER_MESSAGE).addConstraintViolation();
            }
            return result == IdDocuments.Result.VALID;
        }
    }
}
