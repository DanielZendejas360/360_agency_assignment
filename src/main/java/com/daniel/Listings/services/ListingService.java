package com.daniel.Listings.services;

import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ListingService {

    @Autowired
    ListingRepository listingRepository;

    public Listing save(Listing listing) {
        listingRepository.save(listing);
        return listing;
    }
}
