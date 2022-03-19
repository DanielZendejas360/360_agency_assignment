package com.daniel.Listings.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class TierLimitReachedException extends IllegalStateException {
    public TierLimitReachedException(String message) {
        super(message);
    }
}
