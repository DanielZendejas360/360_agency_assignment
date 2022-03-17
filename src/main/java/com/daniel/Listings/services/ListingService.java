package com.daniel.Listings.services;

import com.daniel.Listings.entity.Dealer;
import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.repository.DealerRepository;
import com.daniel.Listings.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ListingService {

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    DealerRepository dealerRepository;

    @Autowired
    TierLimitHandler tierLimitHandler;

    public Listing save(Listing listing) {
        listing.setState(Listing.State.draft);
        return listingRepository.save(listing);
    }

    public Listing update(Listing newListing) {
        UUID id = newListing.getId();
        Optional<Listing> listingOptional = listingRepository.findById(id);
        if (listingOptional.isEmpty())
            throw new IllegalStateException(
                    String.format("Unable to update listing with id '%s'. Listing not found.", id));

        Listing updatedListing = listingOptional.get();
        updatedListing.setVehicle(newListing.getVehicle());
        updatedListing.setPrice(newListing.getPrice());

        return listingRepository.save(updatedListing);
    }

    public List<Listing> getAllWithDealerId(UUID dealerId, Listing.State state) {
        return listingRepository.findByDealerIdAndState(dealerId, state);
    }

    public Listing publish(UUID listingId, TierLimitHandler.Type tierLimitHandling) {
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (listingOptional.isEmpty())
            throw new IllegalStateException(
                    String.format("Unable to publish listing with id '%s'. Listing not found.", listingId));

        Listing listing = listingOptional.get();

        if (listing.isPublished())
            return listing;

        Optional<Dealer> dealerOptional = dealerRepository.findById(listing.getDealerId());
        if (dealerOptional.isEmpty())
            throw new IllegalStateException(
                    String.format("Unable to publish listing for dealer with id %s. Dealer not found.", listing.getDealerId()));

        tierLimitHandler.handle(dealerOptional.get(), listing, tierLimitHandling);

        listing.setState(Listing.State.published);
        return listingRepository.save(listing);
    }

    public Listing unpublish(UUID listingId) {
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (listingOptional.isEmpty())
            throw new IllegalStateException(String.format("Unable to publish listing with id %s. Not found.", listingId));

        Listing listing = listingOptional.get();
        listing.setState(Listing.State.draft);
        return listingRepository.save(listing);
    }
}
