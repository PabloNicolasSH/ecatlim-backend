package org.scoutsdecanarias.ecatlim_backend.shared.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.scoutsdecanarias.ecatlim_backend.shared.utils.IdDocuments.Result.*;

class IdDocumentsTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678Z", "00000000T", "x1234567l", "Y0000000Z", " 12345678z ", "PAA123456", "AB1234567", "123456789", "X12345"})
    void acceptsValidDniNieAndPassports(String document) {
        assertThat(IdDocuments.check(document)).isEqualTo(VALID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "X1234567A"})
    void detectsWrongCheckLetter(String document) {
        assertThat(IdDocuments.check(document)).isEqualTo(WRONG_CHECK_LETTER);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "ABCDEFG", "12345", "1234567890", "12-345-678", "PAA 12345"})
    void rejectsInvalidFormats(String document) {
        assertThat(IdDocuments.check(document)).isEqualTo(INVALID_FORMAT);
    }

    @Test
    void normalizesForStorage() {
        assertThat(IdDocuments.normalize(" x1234567l ")).isEqualTo("X1234567L");
        assertThat(IdDocuments.normalize("  ")).isNull();
    }
}
