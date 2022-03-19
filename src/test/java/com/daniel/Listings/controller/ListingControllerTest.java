package com.daniel.Listings.controller;

import com.daniel.Listings.controllers.ListingController;
import com.daniel.Listings.entity.Listing;
import com.daniel.Listings.exception.ResourceNotFoundException;
import com.daniel.Listings.services.ListingService;
import com.daniel.Listings.services.TierLimitHandler;
import com.daniel.Listings.util.ApiPaths;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.daniel.Listings.entity.Listing.State;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListingController.class)
public class ListingControllerTest {

    @MockBean
    private ListingService listingService;

    @Autowired
    MockMvc mockMvc;

    @Test
    public void create_whenRequestIsValid_serializesCorrectlyAndReturns200() throws Exception {
        UUID dealerId = UUID.randomUUID();

        String vehicle = "Vehicle Name";
        int price = 100_000;

        String requestBody = "{" +
                "\"vehicle\": \"" + vehicle + "\"," +
                "\"price\": " + price +
        "}";

        mockMvc.perform(post(ApiPaths.LISTINGS_CREATE, dealerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());

        ArgumentCaptor<Listing> argumentCaptor = ArgumentCaptor.forClass(Listing.class);
        verify(listingService, atLeastOnce()).create(argumentCaptor.capture());
        Listing serializedListing = argumentCaptor.getValue();
        assertEquals(serializedListing.getDealerId(), dealerId);
        assertEquals(serializedListing.getVehicle(), vehicle);
        assertEquals(serializedListing.getPrice(), price);
    }

    @Test
    public void create_whenRequestBodyIsInvalid_returns400() throws Exception {
        mockMvc.perform(post(ApiPaths.LISTINGS_CREATE, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalid\": \"body\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(ApiPaths.LISTINGS_CREATE, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicle\": \"\", \"price\": 100000}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(ApiPaths.LISTINGS_CREATE, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicle\": \"Vehicle Name\", \"price\": -1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update_whenRequestIsValid_serializesCorrectlyAndReturns200() throws Exception {
        UUID listingId = UUID.randomUUID();
        UUID dealerId = UUID.randomUUID();

        String requestBody = "{" +
                "\"vehicle\": \"Test Vehicle\"," +
                " \"price\": 100000" +
        "}";

        mockMvc.perform(put(ApiPaths.LISTINGS_UPDATE, dealerId, listingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        ArgumentCaptor<Listing> argumentCaptor = ArgumentCaptor.forClass(Listing.class);
        verify(listingService, atLeastOnce()).update(argumentCaptor.capture());
        Listing serializedListing = argumentCaptor.getValue();
        assertEquals(listingId, serializedListing.getId());
        assertEquals(dealerId, serializedListing.getDealerId());
        assertEquals("Test Vehicle", serializedListing.getVehicle());
        assertEquals(100_000, serializedListing.getPrice());
    }

    @Test
    public void update_whenResourceIsNotFound_returns404() throws Exception {
        when(listingService.update(any(Listing.class))).thenThrow(new ResourceNotFoundException("Listing not found"));

        String requestBody = "{" +
                "\"vehicle\": \"Test Vehicle\"," +
                " \"price\": 100000" +
        "}";

        mockMvc.perform(put(ApiPaths.LISTINGS_UPDATE, UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void update_whenRequestBodyIsInvalid_returns400() throws Exception {
        mockMvc.perform(put(ApiPaths.LISTINGS_UPDATE, UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalid\": \"body\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getByDealerIdAndState_whenRequestIsValid_returns200AndListOfListings() throws Exception {
        UUID dealerId = UUID.randomUUID();
        State state = State.draft;

        Listing listing = new Listing();
        listing.setId(UUID.randomUUID());
        listing.setDealerId(UUID.randomUUID());
        listing.setVehicle("Vehicle");
        listing.setPrice(100_000);
        listing.setState(State.draft);
        listing.setCreatedAt(LocalDateTime.now());

        when(listingService.getAllWithDealerIdAndState(dealerId, state)).thenReturn(List.of(listing));

        mockMvc.perform(get(ApiPaths.LISTINGS_GET_BY_DEALER_AND_STATE, dealerId)
                .queryParam("state", State.draft.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(listing.getId().toString()))
                .andExpect(jsonPath("$[0].dealerId").value(listing.getDealerId().toString()))
                .andExpect(jsonPath("$[0].vehicle").value(listing.getVehicle()))
                .andExpect(jsonPath("$[0].price").value(listing.getPrice()))
                .andExpect(jsonPath("$[0].state").value(listing.getState().toString()));
    }

    @Test
    public void getByDealerIdAndState_whenDealerIdDoesNotExist_returns404() throws Exception {
        UUID dealerId = UUID.randomUUID();

        when(listingService.getAllWithDealerIdAndState(eq(dealerId), any(State.class)))
                .thenThrow(new ResourceNotFoundException("Dealer id not found"));

        mockMvc.perform(get(ApiPaths.LISTINGS_GET_BY_DEALER_AND_STATE, dealerId)
                .queryParam("state", State.draft.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getByDealerIdAndState_whenStateIsInvalid_returns400() throws Exception {
        mockMvc.perform(get(ApiPaths.LISTINGS_GET_BY_DEALER_AND_STATE, UUID.randomUUID())
                .queryParam("state", "invalid_state"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getByDealerIdAndState_whenDealerIdIsInvalid_returns400() throws Exception {
        mockMvc.perform(get(ApiPaths.LISTINGS_GET_BY_DEALER_AND_STATE, "invalid_id")
                .queryParam("state", State.published.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void publish_withValidRequests_returns200() throws Exception {
        UUID listingId = UUID.randomUUID();

        mockMvc.perform(post(ApiPaths.LISTINGS_PUBLISH, UUID.randomUUID(), listingId)
                .queryParam("tierLimitHandling", TierLimitHandler.Type.error.toString()))
                .andExpect(status().isOk());

        ArgumentCaptor<UUID> argumentCaptorForListingId = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<TierLimitHandler.Type> argumentCaptorForTierLimitHandling = ArgumentCaptor.forClass(TierLimitHandler.Type.class);
        verify(listingService, atLeastOnce()).publish(argumentCaptorForListingId.capture(), argumentCaptorForTierLimitHandling.capture());
        UUID capturedListingId = argumentCaptorForListingId.getValue();
        TierLimitHandler.Type capturedTierLimitHandling = argumentCaptorForTierLimitHandling.getValue();

        assertEquals(listingId, capturedListingId);
        assertEquals(TierLimitHandler.Type.error, capturedTierLimitHandling);
    }

    @Test
    public void publish_whenResourceIsNotFound_returns404() throws Exception {
        when(listingService.publish(any(UUID.class), any(TierLimitHandler.Type.class)))
                .thenThrow(new ResourceNotFoundException("Resource not found"));

        mockMvc.perform(post(ApiPaths.LISTINGS_PUBLISH, UUID.randomUUID(), UUID.randomUUID())
                .queryParam("tierLimitHandling", TierLimitHandler.Type.error.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void publish_whenRequestIsInvalid_returns400() throws Exception {
        mockMvc.perform(post(ApiPaths.LISTINGS_PUBLISH, UUID.randomUUID(), "InvalidUUID")
                .queryParam("tierLimitHandling", TierLimitHandler.Type.error.toString()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(ApiPaths.LISTINGS_PUBLISH, UUID.randomUUID(), UUID.randomUUID())
                .queryParam("tierLimitHandling", "InvalidType"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void unpublish_withValidRequests_returns200() throws Exception {
        UUID listingId = UUID.randomUUID();

        mockMvc.perform(post(ApiPaths.LISTINGS_UNPUBLISH, UUID.randomUUID(), listingId))
                .andExpect(status().isOk());

        ArgumentCaptor<UUID> argumentCaptorForListingId = ArgumentCaptor.forClass(UUID.class);
        verify(listingService, atLeastOnce()).unpublish(argumentCaptorForListingId.capture());
        UUID capturedListingId = argumentCaptorForListingId.getValue();

        assertEquals(listingId, capturedListingId);
    }

    @Test
    public void unpublish_whenResourceIsNotFound_returns404() throws Exception {
        when(listingService.unpublish(any(UUID.class)))
                .thenThrow(new ResourceNotFoundException("Resource not found"));

        mockMvc.perform(post(ApiPaths.LISTINGS_UNPUBLISH, UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void unpublish_whenRequestIsInvalid_returns400() throws Exception {
        mockMvc.perform(post(ApiPaths.LISTINGS_UNPUBLISH, UUID.randomUUID(), "InvalidUUID"))
                .andExpect(status().isBadRequest());
    }
}

