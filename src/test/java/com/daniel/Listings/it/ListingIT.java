package com.daniel.Listings.it;

import com.daniel.Listings.entity.Dealer;
import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.repository.DealerRepository;
import com.daniel.Listings.repository.ListingRepository;
import com.daniel.Listings.services.TierLimitHandler;
import com.daniel.Listings.util.ApiPaths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:13.2-alpine:///payment",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ListingIT {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private DealerRepository dealerRepository;

    @Autowired
    private ListingRepository listingRepository;

    private static Dealer dealer;

    @BeforeAll
    static void setUp(@Autowired DealerRepository dealerRepository) {
        Dealer d = new Dealer();
        d.setName("Test Dealer");
        d.setTierLimit(3);
        dealer = dealerRepository.save(d);
    }

    @AfterEach
    public void cleanUp(@Autowired ListingRepository listingRepository) {
        listingRepository.deleteAll();
    }

    @Test
    public void create_withValidRequest_returns201AndCorrectlyStoresListingInDB() {
        Listing requestListing = new Listing();
        requestListing.setVehicle("Vehicle");
        requestListing.setPrice(100_000);

        Listing responseListing = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_CREATE)
                        .build(dealer.getId()))
                .bodyValue(requestListing)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Listing.class)
                .returnResult()
                .getResponseBody();

        assertThat(responseListing).isNotNull();
        assertThat(responseListing.getId()).isNotNull();
        assertThat(responseListing.getDealerId()).isEqualTo(dealer.getId());
        assertThat(responseListing.getVehicle()).isEqualTo(requestListing.getVehicle());
        assertThat(responseListing.getPrice()).isEqualTo(requestListing.getPrice());
        assertThat(responseListing.getState()).isEqualTo(Listing.State.draft);

        Optional<Listing> listingInDBOptional = listingRepository.findById(responseListing.getId());
        assertThat(listingInDBOptional.isPresent());
    }

    @Test
    public void create_withNonexistentDealerId_returns404() {
        Listing requestListing = new Listing();
        requestListing.setVehicle("Vehicle");
        requestListing.setPrice(100_000);

        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_CREATE)
                        .build(UUID.randomUUID()))
                .bodyValue(requestListing)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void create_withInvalidRequestBody_returns400() {
        // Invalid vehicle
        Listing requestListing = new Listing();
        requestListing.setVehicle("");
        requestListing.setPrice(100_000);

        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_CREATE)
                        .build(UUID.randomUUID()))
                .bodyValue(requestListing)
                .exchange()
                .expectStatus().isBadRequest();


        // Invalid price
        requestListing = new Listing();
        requestListing.setVehicle("Test Vehicle");
        requestListing.setPrice(-1);

        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_CREATE)
                        .build(UUID.randomUUID()))
                .bodyValue(requestListing)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void update_withValidRequest_returns200AndCorrectlyStoresUpdateInDB() {
        Listing listing = new Listing();
        listing.setDealerId(dealer.getId());
        listing.setVehicle("Vehicle");
        listing.setPrice(100_000);
        listing.setState(Listing.State.published);

        listing = listingRepository.save(listing);
        UUID listingId = listing.getId();

        listing.setPrice(120_000);

        Listing responseListing = webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_UPDATE)
                        .build(dealer.getId(), listingId))
                .bodyValue(listing)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Listing.class)
                .returnResult()
                .getResponseBody();

        assertThat(responseListing).isNotNull();
        assertThat(responseListing.getId()).isEqualTo(listing.getId());
        assertThat(responseListing.getDealerId()).isEqualTo(listing.getDealerId());
        assertThat(responseListing.getVehicle()).isEqualTo(listing.getVehicle());
        assertThat(responseListing.getPrice()).isEqualTo(listing.getPrice());
        assertThat(responseListing.getState()).isEqualTo(listing.getState());

        Optional<Listing> listingInDBOptional = listingRepository.findById(responseListing.getId());
        assertThat(listingInDBOptional.isPresent());
        assertThat(listingInDBOptional.get().getPrice()).isEqualTo(listing.getPrice());
    }

    @Test
    public void update_withNonexistentListingId_returns404() {
        Listing listing = new Listing();
        listing.setDealerId(dealer.getId());
        listing.setVehicle("Vehicle");
        listing.setPrice(100_000);
        listing.setState(Listing.State.published);
        listingRepository.save(listing);

        webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_UPDATE)
                        .build(dealer.getId(), UUID.randomUUID()))
                .bodyValue(listing)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void update_withInvalidUpdate_returns400() {
        Listing listing = new Listing();
        listing.setDealerId(dealer.getId());
        listing.setVehicle("Vehicle");
        listing.setPrice(100_000);
        listing.setState(Listing.State.published);

        listing = listingRepository.save(listing);
        UUID listingId = listing.getId();

        listing.setPrice(-1);

        webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_UPDATE)
                        .build(dealer.getId(), listingId))
                .bodyValue(listing)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void getByDealerIdAndState_whenRequestIsValid_returns200AndCorrectListOfListings() {
        Listing draftListing = new Listing();
        draftListing.setDealerId(dealer.getId());
        draftListing.setVehicle("Unpublished Test Vehicle");
        draftListing.setPrice(100_000);
        draftListing.setState(Listing.State.draft);
        listingRepository.save(draftListing);

        Listing publishedListing = new Listing();
        publishedListing.setDealerId(dealer.getId());
        publishedListing.setVehicle("Published Test Vehicle");
        publishedListing.setPrice(100_000);
        publishedListing.setState(Listing.State.published);
        listingRepository.save(publishedListing);

        // Validate when requesting drafts
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_GET_BY_DEALER_AND_STATE)
                        .queryParam("state", Listing.State.draft)
                        .build(dealer.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isNotEmpty()
                .jsonPath("$[0].dealerId").isEqualTo(dealer.getId().toString())
                .jsonPath("$[0].vehicle").isEqualTo(draftListing.getVehicle())
                .jsonPath("$[0].price").isEqualTo(draftListing.getPrice())
                .jsonPath("$[0].state").isEqualTo(draftListing.getState().toString());

        // Validate when requesting published
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_GET_BY_DEALER_AND_STATE)
                        .queryParam("state", Listing.State.published)
                        .build(dealer.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isNotEmpty()
                .jsonPath("$[0].dealerId").isEqualTo(dealer.getId().toString())
                .jsonPath("$[0].vehicle").isEqualTo(publishedListing.getVehicle())
                .jsonPath("$[0].price").isEqualTo(publishedListing.getPrice())
                .jsonPath("$[0].state").isEqualTo(publishedListing.getState().toString());

        // Validate when requesting with different dealer id
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_GET_BY_DEALER_AND_STATE)
                        .queryParam("state", Listing.State.published)
                        .build(UUID.randomUUID()))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    public void getByDealerIdAndState_whenRequestingInvalidState_returns400() {
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_GET_BY_DEALER_AND_STATE)
                        .queryParam("state", "InvalidState").build(dealer.getId()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void publish_whenTierLimitHasNoBeenReached_returns200AndPublishesListing() {
        Listing draftListing = new Listing();
        draftListing.setDealerId(dealer.getId());
        draftListing.setVehicle("Unpublished Test Vehicle");
        draftListing.setPrice(100_000);
        draftListing.setState(Listing.State.draft);
        Listing createdListing = listingRepository.save(draftListing);

        Listing responseListing = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_PUBLISH)
                        .queryParam("tierLimitHandling", TierLimitHandler.Type.error)
                        .build(dealer.getId(), createdListing.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Listing.class)
                .returnResult()
                .getResponseBody();

        assertThat(responseListing).isNotNull();
        assertThat(responseListing.getState()).isEqualTo(Listing.State.published);

        Optional<Listing> listingInDBOptional = listingRepository.findById(responseListing.getId());
        assertThat(listingInDBOptional.isPresent());
        assertThat(listingInDBOptional.get().getState()).isEqualTo(Listing.State.published);
    }

    @Test
    public void publish_whenTierLimitHasBeenReachedAndClientRequestsError_returns400() {
        Listing publishedListing1 = new Listing();
        publishedListing1.setDealerId(dealer.getId());
        publishedListing1.setVehicle("Published Test Vehicle");
        publishedListing1.setPrice(100_000);
        publishedListing1.setState(Listing.State.published);

        Listing publishedListing2 = new Listing();
        publishedListing2.setDealerId(dealer.getId());
        publishedListing2.setVehicle("Published Test Vehicle");
        publishedListing2.setPrice(100_000);
        publishedListing2.setState(Listing.State.published);

        Listing publishedListing3 = new Listing();
        publishedListing3.setDealerId(dealer.getId());
        publishedListing3.setVehicle("Published Test Vehicle");
        publishedListing3.setPrice(100_000);
        publishedListing3.setState(Listing.State.published);

        listingRepository.save(publishedListing1);
        listingRepository.save(publishedListing2);
        listingRepository.save(publishedListing3);

        Listing draftListing = new Listing();
        draftListing.setDealerId(dealer.getId());
        draftListing.setVehicle("Unpublished Test Vehicle");
        draftListing.setPrice(100_000);
        draftListing.setState(Listing.State.draft);
        Listing createdDraftListing = listingRepository.save(draftListing);

        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_PUBLISH)
                        .queryParam("tierLimitHandling", TierLimitHandler.Type.error)
                        .build(dealer.getId(), createdDraftListing.getId()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void publish_whenTierLimitHasBeenReachedAndClientRequestsOldestReplacement_returns200AndReplaceOldest() {
        Listing publishedListing1 = new Listing();
        publishedListing1.setDealerId(dealer.getId());
        publishedListing1.setVehicle("Published Test Vehicle");
        publishedListing1.setPrice(100_000);
        publishedListing1.setState(Listing.State.published);

        Listing publishedListing2 = new Listing();
        publishedListing2.setDealerId(dealer.getId());
        publishedListing2.setVehicle("Published Test Vehicle");
        publishedListing2.setPrice(100_000);
        publishedListing2.setState(Listing.State.published);

        Listing publishedListing3 = new Listing();
        publishedListing3.setDealerId(dealer.getId());
        publishedListing3.setVehicle("Published Test Vehicle");
        publishedListing3.setPrice(100_000);
        publishedListing3.setState(Listing.State.published);

        listingRepository.save(publishedListing1);
        listingRepository.save(publishedListing2);
        listingRepository.save(publishedListing3);

        Listing draftListing = new Listing();
        draftListing.setDealerId(dealer.getId());
        draftListing.setVehicle("Unpublished Test Vehicle");
        draftListing.setPrice(100_000);
        draftListing.setState(Listing.State.draft);
        Listing createdDraftListing = listingRepository.save(draftListing);

        Listing responseListing = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.LISTINGS_PUBLISH)
                        .queryParam("tierLimitHandling", TierLimitHandler.Type.replaceOldest)
                        .build(dealer.getId(), createdDraftListing.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Listing.class)
                .returnResult()
                .getResponseBody();

        assertThat(responseListing).isNotNull();
        assertThat(responseListing.getState()).isEqualTo(Listing.State.published);

        Optional<Listing> oldestPublishedListingOptional = listingRepository.findById(publishedListing1.getId());
        Listing oldestPublishedListing = oldestPublishedListingOptional.get();
        assertThat(oldestPublishedListing.getState()).isEqualTo(Listing.State.draft);
    }
}
