package com.daniel.Listings.repository;

import com.daniel.Listings.entity.Listing;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository("listingRepository")
public interface ListingRepository extends CrudRepository<Listing, UUID> {

    List<Listing> findByDealerIdAndState(UUID dealerId, Listing.State state);
}
