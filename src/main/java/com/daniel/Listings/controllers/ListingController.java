package com.daniel.Listings.controllers;

import com.daniel.Listings.entity.Listing;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dealers/{dealerId}")
public class ListingController {

    @PostMapping("/listings")
    public void create(@RequestBody Listing listing) {
        throw new UnsupportedOperationException();
    }

    @PutMapping("/listings")
    public void update(@RequestBody Listing listing) {
        throw new UnsupportedOperationException();
    }

    @GetMapping("/listings")
    public void get(@PathVariable UUID dealerId, @RequestParam Listing.State state) {
        throw new UnsupportedOperationException();
    }

    @PostMapping("/listings/{listingId}/publish")
    public void publish(@PathVariable UUID listingId) {
        throw new UnsupportedOperationException();
    }

    @PostMapping("/listings/{listingId}/unpublish")
    public void unpublish(@PathVariable UUID listingId) {
        throw new UnsupportedOperationException();
    }
}
