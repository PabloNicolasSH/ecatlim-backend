package org.scoutsdecanarias.ecatlim_backend.shared.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.ActivityCreationDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.PendingUserFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserMeFormDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static List<String> messages(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).toList();
    }

    @Test
    void pendingUserAcceptsDniNieAndPassport() {
        assertThat(validator.validate(new PendingUserFormDto("Ana", "Pérez", "ana@test.com", "12345678Z", null))).isEmpty();
        assertThat(validator.validate(new PendingUserFormDto("Ana", "Pérez", "ana@test.com", "x1234567l", null))).isEmpty();
        assertThat(validator.validate(new PendingUserFormDto("Ana", "Pérez", "ana@test.com", "PAA123456", null))).isEmpty();
    }

    @Test
    void wrongDniLetterGetsItsOwnMessage() {
        assertThat(messages(validator.validate(new PendingUserFormDto("Ana", "Pérez", "ana@test.com", "12345678A", null))))
                .containsExactly(IdDocuments.CHECK_LETTER_MESSAGE);
    }

    @Test
    void pendingUserRejectsMissingAndMalformedFields() {
        var violations = validator.validate(new PendingUserFormDto("", null, "not-an-email", "123", null));

        assertThat(messages(violations)).contains(
                "El nombre es obligatorio",
                "Los apellidos son obligatorios",
                "El email no tiene un formato válido",
                IdDocuments.FORMAT_MESSAGE);
    }

    @Test
    void pendingUserAdminActionsOnlyRequireTheEmail() {
        var legacyRequest = new PendingUserFormDto("", "", "old@test.com", "legacy-nif", null);

        assertThat(validator.validate(legacyRequest, PendingUserFormDto.ByEmail.class)).isEmpty();
        assertThat(validator.validate(new PendingUserFormDto(null, null, "", null, null), PendingUserFormDto.ByEmail.class))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("El email es obligatorio");
    }

    @Test
    void optionalProfileFieldsMayBeEmptyButNotMalformed() {
        assertThat(validator.validate(new UserMeFormDto("Ana", "Pérez", "", "", null, "", "", ""))).isEmpty();

        var violations = validator.validate(new UserMeFormDto("Ana", "Pérez", "abc", "1234", null, "", "", ""));
        assertThat(messages(violations)).containsExactlyInAnyOrder(ValidationPatterns.PHONE_MESSAGE, IdDocuments.FORMAT_MESSAGE);
    }

    @Test
    void activityDueDateMustBeAfterOpening() {
        LocalDateTime now = LocalDateTime.now();
        var activity = new ActivityCreationDto("Título válido", "Descripción", "FORUM", "AUTOMATIC",
                now, now.minusDays(1), false, 1, false, null, null, List.of(), 2);

        assertThat(messages(validator.validate(activity))).containsExactly("La fecha de entrega debe ser posterior a la de apertura");
    }

    @Test
    void activityRejectsUnknownTypes() {
        LocalDateTime now = LocalDateTime.now();
        var activity = new ActivityCreationDto("Título válido", "Descripción", "HACK", "AUTOMATIC",
                now, now.plusDays(1), false, 1, false, null, null, null, 2);

        assertThat(messages(validator.validate(activity))).containsExactly("El tipo de actividad no es válido");
    }
}
