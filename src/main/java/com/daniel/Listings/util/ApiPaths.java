package com.daniel.Listings.util;

public class ApiPaths {
    public static final String LISTINGS_CREATE = "/dealers/{dealerId}/listings";
    public static final String LISTINGS_UPDATE = "/dealers/{dealerId}/listings/{listingId}";
    public static final String LISTINGS_GET_BY_DEALER_AND_STATE = "/dealers/{dealerId}/listings";
    public static final String LISTINGS_PUBLISH = "/dealers/{dealerId}/listings/{listingId}/publish";
    public static final String LISTINGS_UNPUBLISH = "/dealers/{dealerId}/listings/{listingId}/unpublish";
}
