package com.daniel.Listings.exception;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponseBody {
    private int statusCode;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponseBody(int statusCode, String message, LocalDateTime timestamp) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
    }
}
