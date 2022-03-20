package com.daniel.Listings.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

/**
 * The owner of listings. The field <code>tierLimit</code> represents the maximum number of published ads the dealer
 * can have at the same time.
 *
 * @see Listing
 */
@Data
@Entity
@Table(name = "Dealer")
public class Dealer {
    @Id
    @GeneratedValue
    private UUID id;

    @NotEmpty
    private String name;

    @Min(0)
    private int tierLimit;
}
