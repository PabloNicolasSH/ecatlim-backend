package org.scoutsdecanarias.ecatlim_backend.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Con este email ya se ha solicitado que se le dé de alta en el sistema")
public class PendingUserExistsException extends RuntimeException {
    public PendingUserExistsException(String message) {
        super(message);
    }
}
