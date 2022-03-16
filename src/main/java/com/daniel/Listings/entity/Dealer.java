package com.daniel.Listings.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "Dealer")
public class Dealer {
    @Id
    private UUID id;

    private String name;
    private int tierLimit;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTierLimit() {
        return tierLimit;
    }

    public void setTierLimit(int tierLimit) {
        this.tierLimit = tierLimit;
    }
}
