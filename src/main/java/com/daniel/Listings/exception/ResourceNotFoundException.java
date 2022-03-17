package com.daniel.Listings.exception;

public class ResourceNotFoundException extends IllegalStateException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
