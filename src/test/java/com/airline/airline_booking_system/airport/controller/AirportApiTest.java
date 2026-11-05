package com.airline.airline_booking_system.airport.controller;

import com.airline.airline_booking_system.airport.dtos.response.AirportResponse;
import com.airline.airline_booking_system.airport.enums.AirportStatus;
import com.airline.airline_booking_system.airport.service.AirportService;
import com.airline.airline_booking_system.auth.config.JwtAuthenticationFilter;
import com.airline.airline_booking_system.auth.config.OAuth2LoginSuccessHandler;
import com.airline.airline_booking_system.auth.config.SecurityConfiguration;
import com.airline.airline_booking_system.common.exceptions.AirportCodeAlreadyExistException;
import com.airline.airline_booking_system.common.exceptions.AirportNotFoundException;
import com.airline.airline_booking_system.user.repository.UserRepository;
import com.airline.airline_booking_system.user.service.JwtTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AirportController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, AirportApiTest.TestSecurity.class})
class AirportApiTest {
    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurity { }
    private static final UUID ID = UUID.randomUUID();
    private static final String BODY = """
            {"code":"sgn","name":"Tan Son Nhat","city":"Ho Chi Minh City","country":"Vietnam"}
            """;
    @Autowired MockMvc mvc;
    @MockitoBean AirportService service;
    @MockitoBean JwtTokenService jwtTokenService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean OAuth2LoginSuccessHandler successHandler;
    @MockitoBean ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void publicReadsBindFiltersAndPathAndIncludeStatus() throws Exception {
        when(service.getAirports("Ho Chi Minh", AirportStatus.ACTIVE)).thenReturn(List.of(response()));
        when(service.getAirportById(ID)).thenReturn(response());
        mvc.perform(get("/api/v1/airports").param("keyword", "Ho Chi Minh").param("status", "ACTIVE"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("ACTIVE"));
        mvc.perform(get("/api/v1/airports/{id}", ID)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));
        mvc.perform(get("/api/v1/airports").param("status", "INVALID")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/airports/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void adminCanCreateReplacePatchAndChangeStatus() throws Exception {
        when(service.createAirport(any())).thenReturn(response());
        when(service.updateAirport(eq(ID), any(), anyBoolean())).thenReturn(response());
        when(service.updateStatus(ID, AirportStatus.BLOCKED)).thenReturn(new AirportResponse(
                ID, "SGN", "Tan Son Nhat", "Ho Chi Minh City", "Vietnam", AirportStatus.BLOCKED));
        mvc.perform(asRole(post("/api/v1/airports"), "ADMIN").content(BODY))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/v1/airports/" + ID));
        mvc.perform(asRole(put("/api/v1/airports/{id}", ID), "ADMIN").content(BODY)).andExpect(status().isOk());
        mvc.perform(asRole(patch("/api/v1/airports/{id}", ID), "ADMIN").content("{\"name\":\"New name\"}"))
                .andExpect(status().isOk());
        mvc.perform(asRole(patch("/api/v1/airports/{id}/status", ID), "ADMIN").content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("BLOCKED"));
        verify(service).updateAirport(eq(ID), any(), eq(false));
        verify(service).updateAirport(eq(ID), any(), eq(true));
    }

    @Test
    void writesRequireAdminForEveryMethod() throws Exception {
        for (var request : List.of(post("/api/v1/airports"), put("/api/v1/airports/{id}", ID),
                patch("/api/v1/airports/{id}", ID), patch("/api/v1/airports/{id}/status", ID))) {
            mvc.perform(withCsrf(request).content(BODY)).andExpect(status().isUnauthorized());
            mvc.perform(asRole(request, "CUSTOMER").content(BODY)).andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }

    @Test
    void invalidBodiesAreRejectedBeforeCallingService() throws Exception {
        for (String code : List.of("SG", "SGNN", "S1N", "", " SGN ")) {
            mvc.perform(asRole(post("/api/v1/airports"), "ADMIN").content(BODY.replace("sgn", code)))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(asRole(post("/api/v1/airports"), "ADMIN").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(asRole(patch("/api/v1/airports/{id}", ID), "ADMIN").content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
        for (String body : List.of("{}", "{\"status\":null}", "{\"status\":\"UNKNOWN\"}")) {
            mvc.perform(asRole(patch("/api/v1/airports/{id}/status", ID), "ADMIN").content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test
    void adminWritesStillRejectMissingOrIncorrectCsrfTokens() throws Exception {
        var authentication = new UsernamePasswordAuthenticationToken("test-user", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        mvc.perform(post("/api/v1/airports").sessionAttr("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(authentication))
                .contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
        mvc.perform(asRole(post("/api/v1/airports"), "ADMIN").with(request -> {
                    request.removeHeader("X-XSRF-TOKEN");
                    request.addHeader("X-XSRF-TOKEN", "wrong");
                    return request;
                }).content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void missingAndDuplicateAirportsReturnExpectedErrors() throws Exception {
        when(service.getAirportById(ID)).thenThrow(new AirportNotFoundException(ID));
        when(service.createAirport(any())).thenThrow(new AirportCodeAlreadyExistException("SGN"));
        mvc.perform(get("/api/v1/airports/{id}", ID)).andExpect(status().isNotFound());
        mvc.perform(asRole(post("/api/v1/airports"), "ADMIN").content(BODY))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").exists());
    }

    private AirportResponse response() {
        return new AirportResponse(ID, "SGN", "Tan Son Nhat", "Ho Chi Minh City", "Vietnam", AirportStatus.ACTIVE);
    }

    private MockHttpServletRequestBuilder asRole(MockHttpServletRequestBuilder request, String role) {
        var authentication = new UsernamePasswordAuthenticationToken("test-user", null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        return withCsrf(request).sessionAttr("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(authentication));
    }

    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) {
        String token = "airport-test-csrf-token";
        return request.cookie(new Cookie("XSRF-TOKEN", token))
                .header("X-XSRF-TOKEN", token)
                .contentType(MediaType.APPLICATION_JSON);
    }
}
