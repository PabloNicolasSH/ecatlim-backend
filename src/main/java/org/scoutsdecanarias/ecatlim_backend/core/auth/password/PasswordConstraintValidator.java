package org.scoutsdecanarias.ecatlim_backend.core.auth.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.*;

import java.util.Arrays;


public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    static final String POLICY_MESSAGE = "La contraseña debe tener entre 8 y 256 caracteres, al menos una mayúscula, "
            + "una minúscula, un número y un carácter especial, y no puede contener espacios";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true;
        }
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 256),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule())
        );
        RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) return true;
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(POLICY_MESSAGE).addConstraintViolation();
        return false;
    }
}
