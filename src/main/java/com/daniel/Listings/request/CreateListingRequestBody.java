package com.daniel.Listings.request;

import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.services.TierLimitHandler;

public class CreateListingRequestBody {
    private Listing listing;
    private TierLimitHandler.Type tierLimitHandling;

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public TierLimitHandler.Type getTierLimitHandling() {
        return tierLimitHandling;
    }

    public void setTierLimitHandling(TierLimitHandler.Type tierLimitHandling) {
        this.tierLimitHandling = tierLimitHandling;
    }
}
