package org.scoutsdecanarias.ecatlim_backend.shared.utils;

public final class ValidationPatterns {

    private ValidationPatterns() {}

    public static final String PHONE = "^\\+?[0-9 ]{9,20}$";
    public static final String PHONE_MESSAGE = "El teléfono no tiene un formato válido";
}
