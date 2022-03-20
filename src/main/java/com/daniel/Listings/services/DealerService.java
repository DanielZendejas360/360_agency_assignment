package com.daniel.Listings.services;

import com.daniel.Listings.entity.Dealer;
import com.daniel.Listings.exception.ResourceNotFoundException;
import com.daniel.Listings.repository.DealerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DealerService {
    @Autowired
    DealerRepository dealerRepository;

    public Dealer save(Dealer dealer) {
        return dealerRepository.save(dealer);
    }

    public Dealer update(Dealer newDealer) {
        Optional<Dealer> dealerOptional = dealerRepository.findById(newDealer.getId());
        if (dealerOptional.isEmpty())
            throw new ResourceNotFoundException(
                    String.format("Unable to update dealer. Dealer with id %s not found", newDealer.getId()));

        Dealer updatedDealer = dealerOptional.get();
        updatedDealer.setName(newDealer.getName());
        return dealerRepository.save(updatedDealer);
    }
}
