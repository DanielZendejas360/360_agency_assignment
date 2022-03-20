package com.daniel.Listings.repository;

import com.daniel.Listings.entity.Listing;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository("listingRepository")
public interface ListingRepository extends CrudRepository<Listing, UUID> {

    List<Listing> findByDealerIdAndState(UUID dealerId, Listing.State state);

    @Query(value = "SELECT * FROM listing WHERE state = 'published' AND dealer_id = ?1 ORDER BY created_at ASC LIMIT 1", nativeQuery = true)
    Listing findOldestPublishedListing(UUID id);
}
