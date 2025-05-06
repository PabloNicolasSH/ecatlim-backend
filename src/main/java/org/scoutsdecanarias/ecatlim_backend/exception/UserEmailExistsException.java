package org.scoutsdecanarias.ecatlim_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Este email ya está siendo usado")
public class UserEmailExistsException extends RuntimeException {
    public UserEmailExistsException() {
        super();
    }
}