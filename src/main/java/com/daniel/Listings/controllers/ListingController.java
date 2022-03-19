package com.daniel.Listings.controllers;

import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.services.ListingService;
import com.daniel.Listings.services.TierLimitHandler;
import com.daniel.Listings.util.ApiPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
public class ListingController {

    @Autowired
    ListingService listingService;

    @PostMapping(ApiPaths.LISTINGS_CREATE)
    @ResponseStatus(HttpStatus.CREATED)
    public Listing create(@PathVariable UUID dealerId, @Valid @RequestBody Listing listing) {
        listing.setDealerId(dealerId);

        return listingService.create(listing);
    }

    @PutMapping(ApiPaths.LISTINGS_UPDATE)
    public Listing update(@PathVariable UUID dealerId, @PathVariable UUID listingId, @Valid @RequestBody Listing listing) {
        listing.setId(listingId);
        listing.setDealerId(dealerId);

        return listingService.update(listing);
    }

    @GetMapping(ApiPaths.LISTINGS_GET_BY_DEALER_AND_STATE)
    public List<Listing> getByDealerIdAndState(@PathVariable UUID dealerId, @RequestParam Listing.State state) {
        return listingService.getAllWithDealerIdAndState(dealerId, state);
    }

    @PostMapping(ApiPaths.LISTINGS_PUBLISH)
    public Listing publish(@PathVariable UUID listingId, @RequestParam TierLimitHandler.Type tierLimitHandling) {
        return listingService.publish(listingId, tierLimitHandling);
    }

    @PostMapping(ApiPaths.LISTINGS_UNPUBLISH)
    public Listing unpublish(@PathVariable UUID listingId) {
        return listingService.unpublish(listingId);
    }
}
