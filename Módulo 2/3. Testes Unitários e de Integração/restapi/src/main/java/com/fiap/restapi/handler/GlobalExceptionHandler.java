package com.fiap.restapi.handler;

import com.fiap.restapi.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handlerMethodArgumentValidException(MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();
        for(FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        Collections.sort(errors);
        var errorResponse =
                new ErrorResponse("Validation error", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);

    }
}
