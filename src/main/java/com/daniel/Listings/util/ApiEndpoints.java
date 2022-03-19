package com.daniel.Listings.util;

public class ApiEndpoints {
    public static final String LISTINGS_CREATE = "/api/v1/dealers/{dealerId}/listings";
    public static final String LISTINGS_UPDATE = "/api/v1/dealers/{dealerId}/listings/{listingId}";
    public static final String LISTINGS_GET_BY_DEALER_AND_STATE = "/api/v1/dealers/{dealerId}/listings";
    public static final String LISTINGS_PUBLISH = "/api/v1/dealers/{dealerId}/listings/{listingId}/publish";
    public static final String LISTINGS_UNPUBLISH = "/api/v1/dealers/{dealerId}/listings/{listingId}/unpublish";
}
