package com.airline.airline_booking_system.aircraft.controller;

import com.airline.airline_booking_system.aircraft.dto.*;
import com.airline.airline_booking_system.aircraft.enums.AircraftStatus;
import com.airline.airline_booking_system.aircraft.enums.SeatClass;
import com.airline.airline_booking_system.aircraft.service.AircraftConflictException;
import com.airline.airline_booking_system.aircraft.service.AircraftService;
import com.airline.airline_booking_system.auth.config.JwtAuthenticationFilter;
import com.airline.airline_booking_system.auth.config.OAuth2LoginSuccessHandler;
import com.airline.airline_booking_system.auth.config.SecurityConfiguration;
import com.airline.airline_booking_system.user.repository.UserRepository;
import com.airline.airline_booking_system.user.service.JwtTokenService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AircraftController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, AircraftApiTest.TestSecurity.class})
class AircraftApiTest {
    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurity { }

    private static final UUID ID = UUID.randomUUID();
    private static final String AIRCRAFT = "{\"registrationNo\":\"VN-A321\"}";
    private static final String SEAT = "{\"seatNumber\":\"12A\",\"seatClass\":\"ECONOMY\"}";
    @Autowired MockMvc mvc;
    @MockitoBean AircraftService service;
    @MockitoBean JwtTokenService jwtTokenService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean OAuth2LoginSuccessHandler successHandler;
    @MockitoBean ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void adminAndStaffCanReadAircraftAndPhysicalSeats() throws Exception {
        when(service.listAircrafts()).thenReturn(List.of(response()));
        when(service.getAircraft(ID)).thenReturn(response());
        when(service.listSeats(ID)).thenReturn(List.of(new AircraftSeatListItem("seat-id", "1A", SeatClass.BUSINESS)));
        for (String role : List.of("ADMIN", "STAFF")) {
            mvc.perform(asRole(get("/api/v1/aircrafts"), role)).andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].registrationNo").value("VN-A321"));
            mvc.perform(asRole(get("/api/v1/aircrafts/{id}", ID), role)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
            mvc.perform(asRole(get("/api/v1/aircrafts/{id}/seats", ID), role)).andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].seatClass").value("BUSINESS"))
                    .andExpect(jsonPath("$[0].aircraftId").doesNotExist());
        }
    }

    @Test
    void anonymousAndCustomerCannotReadAnyAircraftEndpoint() throws Exception {
        for (String path : List.of("/api/v1/aircrafts", "/api/v1/aircrafts/" + ID, "/api/v1/aircrafts/" + ID + "/seats")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(asRole(get(path), "CUSTOMER")).andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }

    @Test
    void adminCanCreateUpdateChangeStatusAndAddSeat() throws Exception {
        when(service.createAircraft(any())).thenReturn(response());
        when(service.updateAircraft(eq(ID), any())).thenReturn(response());
        when(service.updateStatus(ID, AircraftStatus.BLOCKED)).thenReturn(
                new AircraftResponse(ID.toString(), "VN-A321", AircraftStatus.BLOCKED));
        when(service.addSeat(eq(ID), any())).thenReturn(
                new AircraftSeatResponse("seat-id", ID.toString(), "12A", SeatClass.ECONOMY));
        mvc.perform(asRole(post("/api/v1/aircrafts"), "ADMIN").content(AIRCRAFT))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/v1/aircrafts/" + ID))
                .andExpect(jsonPath("$.registrationNo").value("VN-A321"));
        mvc.perform(asRole(put("/api/v1/aircrafts/{id}", ID), "ADMIN").content(AIRCRAFT))
                .andExpect(status().isOk());
        mvc.perform(asRole(patch("/api/v1/aircrafts/{id}/status", ID), "ADMIN").content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("BLOCKED"));
        mvc.perform(asRole(post("/api/v1/aircrafts/{id}/seats", ID), "ADMIN").content(SEAT))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.aircraftId").value(ID.toString()))
                .andExpect(jsonPath("$.seatNumber").value("12A"));
    }

    @Test
    void everyWriteRejectsAnonymousStaffAndCustomer() throws Exception {
        for (var request : List.of(post("/api/v1/aircrafts"), put("/api/v1/aircrafts/{id}", ID),
                patch("/api/v1/aircrafts/{id}/status", ID), post("/api/v1/aircrafts/{id}/seats", ID))) {
            mvc.perform(withCsrf(request).content(AIRCRAFT)).andExpect(status().isUnauthorized());
            mvc.perform(asRole(request, "STAFF").content(AIRCRAFT)).andExpect(status().isForbidden());
            mvc.perform(asRole(request, "CUSTOMER").content(AIRCRAFT)).andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }

    @Test
    void missingBlankAndOversizedRegistrationsAreRejected() throws Exception {
        for (String body : List.of("{}", "{\"registrationNo\":null}", "{\"registrationNo\":\"  \"}",
                "{\"registrationNo\":\"" + "X".repeat(51) + "\"}")) {
            mvc.perform(asRole(post("/api/v1/aircrafts"), "ADMIN").content(body)).andExpect(status().isBadRequest());
            mvc.perform(asRole(put("/api/v1/aircrafts/{id}", ID), "ADMIN").content(body)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test
    void onlyThreeSeatClassesAndNonblankSeatNumbersAreAccepted() throws Exception {
        for (String body : List.of("{}", "{\"seatNumber\":\"12A\"}",
                SEAT.replace("ECONOMY", "PREMIUM_ECONOMY"), SEAT.replace("ECONOMY", "UNKNOWN"),
                SEAT.replace("12A", " "), SEAT.replace("12A", "12345678901"))) {
            mvc.perform(asRole(post("/api/v1/aircrafts/{id}/seats", ID), "ADMIN").content(body))
                    .andExpect(status().isBadRequest());
        }
        when(service.addSeat(eq(ID), any())).thenReturn(new AircraftSeatResponse("seat-id", ID.toString(), "12A", SeatClass.ECONOMY));
        for (SeatClass seatClass : SeatClass.values()) {
            mvc.perform(asRole(post("/api/v1/aircrafts/{id}/seats", ID), "ADMIN")
                    .content(SEAT.replace("ECONOMY", seatClass.name()))).andExpect(status().isCreated());
        }
    }

    @Test
    void malformedIdsStatusAndMissingBodiesAreBadRequests() throws Exception {
        mvc.perform(asRole(get("/api/v1/aircrafts/not-a-uuid"), "STAFF")).andExpect(status().isBadRequest());
        mvc.perform(asRole(post("/api/v1/aircrafts"), "ADMIN")).andExpect(status().isBadRequest());
        for (String body : List.of("{}", "{\"status\":null}", "{\"status\":\"UNKNOWN\"}")) {
            mvc.perform(asRole(patch("/api/v1/aircrafts/{id}/status", ID), "ADMIN").content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test
    void missingResourcesAndDuplicatesUse404And409() throws Exception {
        when(service.getAircraft(ID)).thenThrow(new EntityNotFoundException("Aircraft not found"));
        when(service.listSeats(ID)).thenThrow(new EntityNotFoundException("Aircraft not found"));
        when(service.createAircraft(any())).thenThrow(new AircraftConflictException("Registration already exists"));
        when(service.addSeat(eq(ID), any())).thenThrow(new AircraftConflictException("Seat already exists"));
        mvc.perform(asRole(get("/api/v1/aircrafts/{id}", ID), "STAFF")).andExpect(status().isNotFound());
        mvc.perform(asRole(get("/api/v1/aircrafts/{id}/seats", ID), "STAFF")).andExpect(status().isNotFound());
        mvc.perform(asRole(post("/api/v1/aircrafts"), "ADMIN").content(AIRCRAFT))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").exists());
        mvc.perform(asRole(post("/api/v1/aircrafts/{id}/seats", ID), "ADMIN").content(SEAT))
                .andExpect(status().isConflict());
    }

    @Test
    void adminWritesRequireCsrfCookieAndMatchingHeader() throws Exception {
        var authentication = new UsernamePasswordAuthenticationToken("test-user", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        mvc.perform(post("/api/v1/aircrafts").contentType(MediaType.APPLICATION_JSON).content(AIRCRAFT)
                .sessionAttr("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(authentication)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    private AircraftResponse response() {
        return new AircraftResponse(ID.toString(), "VN-A321", AircraftStatus.ACTIVE);
    }

    private MockHttpServletRequestBuilder asRole(MockHttpServletRequestBuilder request, String role) {
        var authentication = new UsernamePasswordAuthenticationToken("test-user", null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        return withCsrf(request).sessionAttr("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(authentication));
    }

    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) {
        return request.cookie(new Cookie("XSRF-TOKEN", "aircraft-test-csrf"))
                .header("X-XSRF-TOKEN", "aircraft-test-csrf").contentType(MediaType.APPLICATION_JSON);
    }
}
