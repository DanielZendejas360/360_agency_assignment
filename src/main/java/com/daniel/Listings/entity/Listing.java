package com.daniel.Listings.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "Listing")
public class Listing {

    public enum State { draft, published }

    @Id
    @GeneratedValue
    private UUID id;

    private UUID dealerId;

    @NotEmpty
    private String vehicle;

    @Min(0)
    private int price;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private State state = State.draft;

    @JsonIgnore
    public boolean isPublished() {
        return this.state == State.published;
    }
}
