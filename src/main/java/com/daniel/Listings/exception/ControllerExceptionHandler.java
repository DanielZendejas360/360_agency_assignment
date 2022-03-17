package com.daniel.Listings.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponseBody> handleResourceNotFound(ResourceNotFoundException e, WebRequest request) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ErrorResponseBody errorResponseBody = new ErrorResponseBody(httpStatus.value(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponseBody, httpStatus);
    }

    @ExceptionHandler(value = {TierLimitReachedException.class})
    public ResponseEntity<ErrorResponseBody> handleTierLimitReached(TierLimitReachedException e, WebRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ErrorResponseBody errorResponseBody = new ErrorResponseBody(httpStatus.value(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponseBody, httpStatus);
    }
}
