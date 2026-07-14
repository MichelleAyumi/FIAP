package com.fiap.techchallengue.infrastructure.web;

import com.fiap.techchallengue.application.exception.BusinessException;
import com.fiap.techchallengue.application.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            Map<String, String> fields) {
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception) {
        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                Map.of());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessError(
            BusinessException exception) {
        return createErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(
            MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.putIfAbsent(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()));

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos",
                fieldErrors);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String message,Map<String, String> fieldErrors) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                fieldErrors);

        return ResponseEntity.status(status).body(errorResponse);
    }
}
