package com.daniel.Listings.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Entity
@Table(name = "Dealer")
public class Dealer {
    @Id
    private UUID id;

    private String name;
    private int tierLimit;
}
