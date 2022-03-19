package com.daniel.Listings.services;

import com.daniel.Listings.entity.Dealer;
import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.exception.ResourceNotFoundException;
import com.daniel.Listings.repository.DealerRepository;
import com.daniel.Listings.repository.ListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ListingService {

    private static final Logger log = LoggerFactory.getLogger(ListingService.class);

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    DealerRepository dealerRepository;

    @Autowired
    TierLimitHandler tierLimitHandler;

    public Listing create(Listing listing) {
        listing.setState(Listing.State.draft);

        log.info(String.format("Creating listing %s", listing));

        return listingRepository.save(listing);
    }

    public Listing update(Listing newListing) {
        UUID id = newListing.getId();
        Optional<Listing> listingOptional = listingRepository.findById(id);
        if (listingOptional.isEmpty())
            throw new ResourceNotFoundException(
                    String.format("Unable to update listing with id '%s'. Listing not found.", id));

        Listing updatedListing = listingOptional.get();
        updatedListing.setVehicle(newListing.getVehicle());
        updatedListing.setPrice(newListing.getPrice());

        log.info(String.format("Updating listing %s", updatedListing));

        return listingRepository.save(updatedListing);
    }

    public List<Listing> getAllWithDealerIdAndSstate(UUID dealerId, Listing.State state) {
        return listingRepository.findByDealerIdAndState(dealerId, state);
    }

    public Listing publish(UUID listingId, TierLimitHandler.Type tierLimitHandling) {
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (listingOptional.isEmpty())
            throw new ResourceNotFoundException(
                    String.format("Unable to publish listing with id '%s'. Listing not found.", listingId));

        Listing listing = listingOptional.get();

        if (listing.isPublished()) {
            log.warn(String.format("Tried publishing listing %s but it is already published", listing));
            return listing;
        }

        Optional<Dealer> dealerOptional = dealerRepository.findById(listing.getDealerId());
        if (dealerOptional.isEmpty())
            throw new ResourceNotFoundException(
                    String.format("Unable to publish listing for dealer with id %s. Dealer not found.", listing.getDealerId()));

        tierLimitHandler.handle(dealerOptional.get(), listing, tierLimitHandling);

        log.info(String.format("Publishing listing %s", listing));

        listing.setState(Listing.State.published);
        return listingRepository.save(listing);
    }

    public Listing unpublish(UUID listingId) {
        Optional<Listing> listingOptional = listingRepository.findById(listingId);
        if (listingOptional.isEmpty())
            throw new ResourceNotFoundException(String.format("Unable to publish listing with id %s. Not found.", listingId));

        Listing listing = listingOptional.get();

        log.info(String.format("Unpublishing listing %s", listing));

        listing.setState(Listing.State.draft);
        return listingRepository.save(listing);
    }
}
