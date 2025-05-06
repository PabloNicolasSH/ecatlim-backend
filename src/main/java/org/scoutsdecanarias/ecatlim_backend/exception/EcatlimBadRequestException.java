package org.scoutsdecanarias.ecatlim_backend.exception;

public class EcatlimBadRequestException extends RuntimeException {
    public EcatlimBadRequestException(String message) {
        super(message);
    }
}
