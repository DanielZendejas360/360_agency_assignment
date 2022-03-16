package com.daniel.Listings.repository;

import com.daniel.Listings.entity.Dealer;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface DealerRepository extends CrudRepository<Dealer, UUID> {
}
