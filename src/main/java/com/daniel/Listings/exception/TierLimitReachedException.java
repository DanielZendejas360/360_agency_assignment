package com.daniel.Listings.exception;

public class TierLimitReachedException extends IllegalStateException {
    public TierLimitReachedException(String message) {
        super(message);
    }
}
