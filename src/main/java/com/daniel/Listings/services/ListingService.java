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

/**
 * Contains business logic for everything related to management of {@link Listing}s.
 */
@Service
public class ListingService {

    private static final Logger log = LoggerFactory.getLogger(ListingService.class);

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    DealerRepository dealerRepository;

    @Autowired
    TierLimitHandler tierLimitHandler;

    /**
     * Persists a listing in the DB. It overrides the <code>state</code> field to <code>draft</code>. If the
     * {@link Dealer} id cannot be found in the DB, then throws a {@link ResourceNotFoundException}.
     *
     * @param listing The listing to be created
     * @return The listing received but with the added <code>id</code> field and state <code>draft</code>.
     * @throws ResourceNotFoundException if the dealer id provided cannot be found
     */
    public Listing create(Listing listing) {
        listing.setState(Listing.State.draft);

        Optional<Dealer> dealerOptional = dealerRepository.findById(listing.getDealerId());
        if (dealerOptional.isEmpty())
            throw new ResourceNotFoundException(
                    String.format("Unable to create listing. Dealer with id %s not found" , listing.getDealerId()));

        log.info(String.format("Creating listing %s", listing));

        return listingRepository.save(listing);
    }

    /**
     * Updates the received listing with provided changes. Note that it only updates the fields <code>vehicle</code>
     * and <code>price</code>. Most notably, the field <code>state</code> cannot be changed through here. If the client
     * wants to publish/unpublish a listing, they should use the proper actions for that.
     *
     * @param newListing The listing to be updated
     * @return The listing received after it was persisted.
     * @throws ResourceNotFoundException if the provided listing does not exist
     */
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

    /**
     * Returns the listings related to the given {@link Dealer} id and that have the given {@link Listing.State}.
     *
     * @param dealerId The {@link UUID} id of the {@link Dealer}
     * @param state The {@link Listing.State} to be queried
     * @return A list of {@link Listing} objects
     */
    public List<Listing> getAllWithDealerIdAndState(UUID dealerId, Listing.State state) {
        return listingRepository.findByDealerIdAndState(dealerId, state);
    }

    /**
     * Publishes the {@link Listing} with the provided {@link UUID} id. Each {@link Dealer} has a tier limit, that
     * dictates how many simultaneous published listings they can have. The parameter {@link TierLimitHandler.Type}
     * represents the action to take if the tier limit is surpassed.
     *
     * @param listingId The {@link UUID} id of the listing to be published
     * @param tierLimitHandling The {@link TierLimitHandler.Type} parameter to decide which action to take in case the
     *                          tier limit has been reached
     * @return The {@link Listing} published, or an error message if the tier limit is reached
     * @see TierLimitHandler
     * @throws ResourceNotFoundException if the listing of the dealer of the ids provided cannot be found
     */
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

    /**
     * Unpublishes the {@link Listing} with the id provided.
     *
     * @param listingId The {@link UUID} id of the listing to unpublish
     * @return The unpublished listing.
     * @throws ResourceNotFoundException when the provided listing with the id provided is not found
     */
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
