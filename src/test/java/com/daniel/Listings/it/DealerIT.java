package com.daniel.Listings.it;

import com.daniel.Listings.entity.Dealer;
import com.daniel.Listings.repository.DealerRepository;
import com.daniel.Listings.util.ApiPaths;
import org.junit.jupiter.api.AfterEach;
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
public class DealerIT {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private DealerRepository dealerRepository;

    @AfterEach
    public void cleanUp(@Autowired DealerRepository dealerRepository) {
        dealerRepository.deleteAll();
    }

    @Test
    public void create_withValidRequest_returns200AndCreatesDealer() {
        Dealer requestDealer = new Dealer();
        requestDealer.setName("Dealer");
        requestDealer.setTierLimit(3);

        Dealer responseDealer = webClient.post()
                .uri(uriBuilder -> uriBuilder.path(ApiPaths.DEALERS_CREATE).build())
                .bodyValue(requestDealer)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Dealer.class)
                .returnResult()
                .getResponseBody();

        assertThat(responseDealer).isNotNull();
        assertThat(responseDealer.getId()).isNotNull();
        assertThat(responseDealer.getName()).isEqualTo(requestDealer.getName());
        assertThat(responseDealer.getTierLimit()).isEqualTo(requestDealer.getTierLimit());

        Optional<Dealer> dealerOptional = dealerRepository.findById(responseDealer.getId());
        assertThat(dealerOptional.isPresent());
    }

    @Test
    public void create_withInvalidRequest_returns400() {
        Dealer invalidRequestDealer = new Dealer();
        invalidRequestDealer.setName("");
        invalidRequestDealer.setTierLimit(3);

        webClient.post()
                .uri(uriBuilder -> uriBuilder.path(ApiPaths.DEALERS_CREATE).build())
                .bodyValue(invalidRequestDealer)
                .exchange()
                .expectStatus().isBadRequest();

        invalidRequestDealer = new Dealer();
        invalidRequestDealer.setName("Dealer");
        invalidRequestDealer.setTierLimit(-1);

        webClient.post()
                .uri(uriBuilder -> uriBuilder.path(ApiPaths.DEALERS_CREATE).build())
                .bodyValue(invalidRequestDealer)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void update_withValidRequest_returns200AndStoresChanges() {
        Dealer originalDealer = new Dealer();
        originalDealer.setName("Dealer");
        originalDealer.setTierLimit(3);
        originalDealer = dealerRepository.save(originalDealer);

        UUID dealerId = originalDealer.getId();

        Dealer editedDealer = new Dealer();
        editedDealer.setId(dealerId);
        editedDealer.setName("New Name Dealer");

        Dealer responseDealer = webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.DEALERS_UPDATE)
                        .build(dealerId))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(editedDealer)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Dealer.class)
                .returnResult()
                .getResponseBody();

        assertThat(responseDealer).isNotNull();
        assertThat(responseDealer.getName()).isEqualTo(editedDealer.getName());

        Optional<Dealer> dealerInDbOptional = dealerRepository.findById(responseDealer.getId());
        assertThat(dealerInDbOptional.isPresent());
        assertThat(dealerInDbOptional.get().getName()).isEqualTo(editedDealer.getName());
    }

    @Test
    public void update_withNonExistentDealer_returns404() {
        Dealer dealer = new Dealer();
        dealer.setName("Dealer");
        dealer.setTierLimit(3);

        webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.DEALERS_UPDATE)
                        .build(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dealer)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void update_withInvalidUpdate_returns400() {
        Dealer dealer = new Dealer();
        dealer.setName("Dealer");
        dealer.setTierLimit(3);
        dealer = dealerRepository.save(dealer);

        UUID dealerId = dealer.getId();

        Dealer editedDealer = new Dealer();
        editedDealer.setId(dealerId);
        editedDealer.setName("");
        editedDealer.setTierLimit(5);

        webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(ApiPaths.DEALERS_UPDATE)
                        .build(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(editedDealer)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
