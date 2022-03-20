package com.daniel.Listings.exception;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Error message to be sent back to the client if an exception was caught in the {@link com.daniel.Listings.controllers.ListingController}.
 */
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
