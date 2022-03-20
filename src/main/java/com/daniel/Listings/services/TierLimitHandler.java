package com.daniel.Listings.services;

import com.daniel.Listings.entity.Dealer;
import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.exception.TierLimitReachedException;
import com.daniel.Listings.repository.ListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * In charge of deciding what to do when a publish request goes over the tier limit of a dealer.
 */
@Component
public class TierLimitHandler {

    private static final Logger log = LoggerFactory.getLogger(TierLimitHandler.class);

    @Autowired
    private ListingRepository listingRepository;

    public enum Type { error, replaceOldest }

    /**
     * Handles the case where the tier limit has been reached. Currently there are two ways to handle this:
     * <ul>
     *     <li><b>Throw an error.</b> If the tier limit has been reached then an TierLimitReachedException is thrown.</li>
     *     <li><b>Replace the oldest published listing with the new one.</b> We search for the listing with the oldest
     *     <code>created_at</code> timestamp, set its state to "draft" and then publish the new one. This is an
     *     attempt to keep the dealer's published listings under their tier limit.</li>
     * <ul/>
     *
     * @see TierLimitReachedException
     * @param dealer The Dealer object that is publishing the listing
     * @param listing The Listing object to be published
     * @param tierLimitHandling An enum value representing what decision should be taken if the tier limit is reached.
     */
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

        throw new TierLimitReachedException(String.format(
            "Tier limit reached. Current published listings are %d and the dealer's tier limit is %d",
            publishedListings.size(),
            dealer.getTierLimit()));
    }

    private void replaceOldestIfTierLimitIsReached(Dealer dealer, Listing newListing) {
        List<Listing> publishedListings = listingRepository.findByDealerIdAndState(dealer.getId(), Listing.State.published);

        if (dealer.getTierLimit() > publishedListings.size())
            return;

        Listing oldestPublishedListing = listingRepository.findOldestPublishedListing(dealer.getId());

        log.info(String.format("Replacing oldest published listing %s", oldestPublishedListing));

        oldestPublishedListing.setState(Listing.State.draft);
        listingRepository.save(oldestPublishedListing);
    }
}
