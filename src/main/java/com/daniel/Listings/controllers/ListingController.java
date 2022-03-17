package com.daniel.Listings.controllers;

import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.services.ListingService;
import com.daniel.Listings.services.TierLimitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dealers/{dealerId}")
public class ListingController {

    @Autowired
    ListingService listingService;

    @PostMapping("/listings")
    public Listing create(@PathVariable UUID dealerId, @RequestBody Listing listing) {
        listing.setDealerId(dealerId);

        return listingService.save(listing);
    }

    @PutMapping("/listings/{listingId}")
    public Listing update(@PathVariable UUID listingId, @PathVariable UUID dealerId, @RequestBody Listing listing) {
        listing.setId(listingId);
        listing.setDealerId(dealerId);

        return listingService.update(listing);
    }

    @GetMapping("/listings")
    public List<Listing> get(@PathVariable UUID dealerId, @RequestParam Listing.State state) {
        return listingService.getAllWithDealerId(dealerId, state);
    }

    @PostMapping("/listings/{listingId}/publish")
    public Listing publish(@PathVariable UUID listingId, @RequestParam TierLimitHandler.Type tierLimitHandling) {
        return listingService.publish(listingId, tierLimitHandling);
    }

    @PostMapping("/listings/{listingId}/unpublish")
    public Listing unpublish(@PathVariable UUID listingId) {
        return listingService.unpublish(listingId);
    }
}
