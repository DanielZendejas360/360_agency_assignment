package com.daniel.Listings.services;

import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListingService {

    @Autowired
    ListingRepository listingRepository;

    public Listing save(Listing listing) {
        return listingRepository.save(listing);
    }

    public List<Listing> getAllWithDealerId(UUID dealerId, Listing.State state) {
        return listingRepository.findByDealerIdAndState(dealerId, state);
    }
}
