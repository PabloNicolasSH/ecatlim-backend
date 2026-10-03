package org.scoutsdecanarias.ecatlim_backend.shared.utils;

import java.util.Locale;
import java.util.regex.Pattern;

public final class IdDocuments {

    public enum Result { VALID, WRONG_CHECK_LETTER, INVALID_FORMAT }

    public static final String FORMAT_MESSAGE =
            "Introduce un DNI, NIE o pasaporte válido (ej.: 12345678Z, X1234567L o PAA123456)";
    public static final String CHECK_LETTER_MESSAGE = "La letra del DNI/NIE no es correcta";

    private static final String CHECK_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final Pattern DNI = Pattern.compile("^\\d{8}[A-Z]$");
    private static final Pattern NIE = Pattern.compile("^[XYZ]\\d{7}[A-Z]$");
    private static final Pattern PASSPORT = Pattern.compile("^(?=.*\\d)[A-Z0-9]{6,9}$");
    private static final Pattern DNI_WITHOUT_LETTER = Pattern.compile("^\\d{8}$");

    private IdDocuments() {}

    public static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public static Result check(String value) {
        String document = normalize(value);
        if (document == null) {
            return Result.INVALID_FORMAT;
        }

        if (DNI.matcher(document).matches()) {
            return hasValidLetter(document.substring(0, 8), document.charAt(8));
        }
        if (NIE.matcher(document).matches()) {
            String digits = "XYZ".indexOf(document.charAt(0)) + document.substring(1, 8);
            return hasValidLetter(digits, document.charAt(8));
        }
        if (DNI_WITHOUT_LETTER.matcher(document).matches()) {
            return Result.INVALID_FORMAT;
        }
        return PASSPORT.matcher(document).matches() ? Result.VALID : Result.INVALID_FORMAT;
    }

    private static Result hasValidLetter(String digits, char letter) {
        char expected = CHECK_LETTERS.charAt(Integer.parseInt(digits) % 23);
        return expected == letter ? Result.VALID : Result.WRONG_CHECK_LETTER;
    }
}
