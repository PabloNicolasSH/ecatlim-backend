package org.scoutsdecanarias.ecatlim_backend.core.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class EcatlimException extends RuntimeException {

    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public EcatlimException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public EcatlimException(String message) {
        super(message);
    }
}
