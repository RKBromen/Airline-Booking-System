package com.airline.airline_booking_system.flight.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.airline.airline_booking_system.airport.entity.Airports;
import com.airline.airline_booking_system.airport.repository.AirportRepository;
import com.airline.airline_booking_system.flight.dto.FlightRequest;
import com.airline.airline_booking_system.flight.enums.FlightStatus;
import com.airline.airline_booking_system.flight.repository.FlightRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class FlightServiceTest {
    private FlightRepository flightRepository;
    private AirportRepository airportRepository;
    private FlightService service;
    private Airports departureAirport;
    private Airports arrivalAirport;

    @BeforeEach
    void setUp() {
        flightRepository = mock(FlightRepository.class);
        airportRepository = mock(AirportRepository.class);
        service = new FlightService(flightRepository, airportRepository);

        departureAirport = airport(UUID.randomUUID(), "SGN");
        arrivalAirport = airport(UUID.randomUUID(), "HAN");
        when(airportRepository.findById(departureAirport.getId())).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById(arrivalAirport.getId())).thenReturn(Optional.of(arrivalAirport));
        when(flightRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createNormalizesFlightNumberAndDefaultsStatusToActive() {
        var result = service.createFlight(new FlightRequest(" ab123 ", departureAirport.getId(), arrivalAirport.getId()));

        assertNotNull(result.id());
        assertEquals("AB123", result.flightNumber());
        assertEquals(departureAirport.getId(), result.departureAirportId());
        assertEquals("SGN", result.departureAirportCode());
        assertEquals(arrivalAirport.getId(), result.arrivalAirportId());
        assertEquals("HAN", result.arrivalAirportCode());
        assertEquals(FlightStatus.ACTIVE, result.status());
        verify(flightRepository).findFlightByNumber("AB123", null);
    }

    @Test
    void duplicateFlightNumberReturnsConflictWithoutSaving() {
        when(flightRepository.findFlightByNumber("AB123", null)).thenReturn(Optional.of("existing-flight"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.createFlight(new FlightRequest("ab123", departureAirport.getId(), arrivalAirport.getId())));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(flightRepository, never()).saveAndFlush(any());
    }

    @Test
    void duplicateRouteReturnsConflictAndSameAirportIsInvalid() {
        when(flightRepository.findFlightOnRoute(departureAirport.getId(), arrivalAirport.getId(), null))
                .thenReturn(Optional.of("existing-flight"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.createFlight(new FlightRequest("AB123", departureAirport.getId(), arrivalAirport.getId())));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertThrows(IllegalArgumentException.class, () -> service.createFlight(
                new FlightRequest("AB124", departureAirport.getId(), departureAirport.getId())));
        verify(flightRepository, never()).saveAndFlush(any());
    }

    @Test
    void missingAirportIsReported() {
        UUID missingId = UUID.randomUUID();

        assertThrows(EntityNotFoundException.class, () -> service.createFlight(
                new FlightRequest("AB123", missingId, arrivalAirport.getId())));
        verify(flightRepository, never()).saveAndFlush(any());
    }

    private Airports airport(UUID id, String code) {
        Airports airport = new Airports();
        airport.setId(id);
        airport.setCode(code);
        return airport;
    }
}
