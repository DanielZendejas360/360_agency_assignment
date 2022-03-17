package com.daniel.Listings.services;

import com.daniel.Listings.entity.Dealer;
import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.repository.ListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TierLimitHandler {

    private static final Logger log = LoggerFactory.getLogger(TierLimitHandler.class);

    @Autowired
    private ListingRepository listingRepository;

    public enum Type { error, replaceOldest }

    public void handle(Dealer dealer, Listing listing, TierLimitHandler.Type tierLimitHandling) {
        switch (tierLimitHandling) {
            case error:
                throwErrorIfTierLimitIsReached(dealer);
                break;
            case replaceOldest:
                replaceOldestIfTierLimitIsReached(dealer, listing);
                break;
        }
    }

    private void throwErrorIfTierLimitIsReached(Dealer dealer) {
        List<Listing> publishedListings = listingRepository.findByDealerIdAndState(dealer.getId(), Listing.State.published);

        boolean tierLimitReached = dealer.getTierLimit() <= publishedListings.size();
        if (!tierLimitReached)
            return;

        throw new IllegalStateException(String.format(
                "Tier limit reached. Current published listings are %d and the dealer's tier limit is %d",
                publishedListings.size(),
                dealer.getTierLimit()));
    }

    private void replaceOldestIfTierLimitIsReached(Dealer dealer, Listing newListing) {
        List<Listing> publishedListings = listingRepository.findByDealerIdAndState(dealer.getId(), Listing.State.published);

        if (dealer.getTierLimit() > publishedListings.size())
            return;

        if (publishedListings.size() == 0)
            return;

        Listing oldestPublishedListing = listingRepository.findOldestPublishedListing();

        log.info(String.format("Replacing oldest published listing %s", oldestPublishedListing));

        oldestPublishedListing.setState(Listing.State.draft);
        listingRepository.save(oldestPublishedListing);
    }
}
