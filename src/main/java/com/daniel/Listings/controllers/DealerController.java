package com.daniel.Listings.controllers;

import com.daniel.Listings.entity.Dealer;
import com.daniel.Listings.services.DealerService;
import com.daniel.Listings.util.ApiPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
public class DealerController {

    @Autowired
    DealerService dealerService;

    @PostMapping(ApiPaths.DEALERS_CREATE)
    @ResponseStatus(HttpStatus.CREATED)
    public Dealer create(@RequestBody @Valid Dealer dealer) {
        return dealerService.save(dealer);
    }

    @PutMapping(ApiPaths.DEALERS_UPDATE)
    public Dealer update(@RequestBody @Valid Dealer dealer, @PathVariable UUID dealerId) {
        dealer.setId(dealerId);
        return dealerService.update(dealer);
    }
}
