package org.scoutsdecanarias.ecatlim_backend.core.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {
    private final String maxFileSize;

    public ExceptionControllerAdvice(
        @Value("${spring.servlet.multipart.max-file-size}") String maxFileSize
    ) {
        this.maxFileSize = maxFileSize;
    }

    @ExceptionHandler(EcatlimException.class)
    public ResponseEntity<EcatlimErrorResponse> handleEcatlimException(EcatlimException ex) {
        return ResponseEntity
            .status(ex.getStatus())
            .body(new EcatlimErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<EcatlimErrorResponse> handleEcatlimException(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new EcatlimErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<EcatlimErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        log.warn("Authorization Denied Exception: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new EcatlimErrorResponse("No tienes permisos para realizar esta acción"));
    }


    @Override
    protected @Nullable ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        log.warn("Maximum Upload Size Exceeded Exception: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(new EcatlimErrorResponse("El archivo es demasiado grande. El tamaño máximo permitido es de %s.".formatted(maxFileSize)));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        List<String> messages = ex.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .toList();
        return validationError(messages);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        List<String> messages = ex.getAllErrors().stream()
            .map(MessageSourceResolvable::getDefaultMessage)
            .toList();
        return validationError(messages);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        return validationError(ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList());
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        log.warn("Http Message Not Readable Exception: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new EcatlimErrorResponse("Los datos enviados no tienen un formato válido"));
    }

    private ResponseEntity<Object> validationError(List<String> messages) {
        String message = messages.stream()
            .filter(Objects::nonNull)
            .distinct()
            .limit(5)
            .collect(Collectors.joining(". "));
        log.warn("Validation failed: {}", message);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new EcatlimErrorResponse(message.isBlank() ? "Los datos enviados no son válidos" : message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleUnexpectedException(Exception ex) {
        log.error("Unknown Internal Exception: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .build();
    }
}
