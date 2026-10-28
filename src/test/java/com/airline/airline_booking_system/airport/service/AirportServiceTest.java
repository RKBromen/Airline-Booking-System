package com.airline.airline_booking_system.airport.service;

import com.airline.airline_booking_system.airport.dtos.request.AirportRequest;
import com.airline.airline_booking_system.airport.dtos.request.UpdateAirportRequest;
import com.airline.airline_booking_system.airport.entity.Airports;
import com.airline.airline_booking_system.airport.enums.AirportStatus;
import com.airline.airline_booking_system.airport.repository.AirportRepository;
import com.airline.airline_booking_system.common.exceptions.AirportCodeAlreadyExistException;
import com.airline.airline_booking_system.common.exceptions.AirportNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AirportServiceTest {
    private AirportRepository repository;
    private AirportService service;
    private Airports airport;

    @BeforeEach
    void setUp() {
        repository = mock(AirportRepository.class);
        service = new AirportService(repository);
        airport = new Airports();
        airport.setId(UUID.randomUUID());
        airport.setCode("SGN");
        airport.setName("Tan Son Nhat");
        airport.setCity("Ho Chi Minh City");
        airport.setCountry("Vietnam");
        airport.setStatus(AirportStatus.ACTIVE);
        when(repository.findById(airport.getId())).thenReturn(Optional.of(airport));
        when(repository.save(any(Airports.class))).thenAnswer(call -> call.getArgument(0));
        when(repository.saveAndFlush(any(Airports.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void creationNormalizesCodeGeneratesIdAndDefaultsToActive() {
        var result = service.createAirport(new AirportRequest("sgn", " Tan Son Nhat ", " HCMC ", " Vietnam "));
        assertNotNull(result.id());
        assertEquals("SGN", result.code());
        assertEquals("Tan Son Nhat", result.name());
        assertEquals("HCMC", result.city());
        assertEquals("Vietnam", result.country());
        assertEquals(AirportStatus.ACTIVE, result.status());
        verify(repository).existsByCodeIgnoreCase("SGN");
        verify(repository, never()).existsByCityAndCountry(any(), any());
        verify(repository, never()).existsByName(any());
    }

    @Test
    void duplicateCodeIsRejectedBeforeSaving() {
        when(repository.existsByCodeIgnoreCase("SGN")).thenReturn(true);
        assertThrows(AirportCodeAlreadyExistException.class,
                () -> service.createAirport(new AirportRequest("sgn", "Name", "City", "Country")));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void concurrentDuplicateCodeIsAlsoTranslatedToConflict() {
        var constraint = new org.hibernate.exception.ConstraintViolationException(
                "Duplicate", new SQLException("Duplicate"), "uk_airports_code");
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("Duplicate", constraint));
        assertThrows(AirportCodeAlreadyExistException.class,
                () -> service.createAirport(new AirportRequest("SGN", "Name", "City", "Country")));
    }

    @Test
    void missingAirportIsReportedForReadsAndUpdates() {
        UUID missing = UUID.randomUUID();
        assertThrows(AirportNotFoundException.class, () -> service.getAirportById(missing));
        assertThrows(AirportNotFoundException.class, () -> service.updateStatus(missing, AirportStatus.BLOCKED));
        assertThrows(AirportNotFoundException.class,
                () -> service.updateAirport(missing, new UpdateAirportRequest(null, "Name", null, null), true));
    }

    @Test
    void partialUpdatePreservesOmittedFieldsAndCode() {
        var result = service.updateAirport(airport.getId(), new UpdateAirportRequest(null, " New name ", null, null), true);
        assertEquals("New name", result.name());
        assertEquals("SGN", result.code());
        assertEquals("Ho Chi Minh City", result.city());
        assertEquals("Vietnam", result.country());
        assertEquals(AirportStatus.ACTIVE, result.status());
    }

    @Test
    void replacementUpdatesAllEditableFields() {
        var result = service.updateAirport(airport.getId(),
                new UpdateAirportRequest("sgn", " New name ", " New city ", " New country "), false);
        assertEquals("SGN", result.code());
        assertEquals("New name", result.name());
        assertEquals("New city", result.city());
        assertEquals("New country", result.country());
        assertEquals(AirportStatus.ACTIVE, result.status());
        verify(repository).save(airport);
    }

    @Test
    void replacementRequiresAllEditableFieldsAndRejectsCodeChanges() {
        assertThrows(IllegalArgumentException.class, () -> service.updateAirport(airport.getId(),
                new UpdateAirportRequest(null, "Name", null, null), false));
        assertThrows(IllegalArgumentException.class, () -> service.updateAirport(airport.getId(),
                new UpdateAirportRequest("HAN", "Name", "City", "Country"), true));
        verify(repository, never()).save(any());
        assertEquals("SGN", airport.getCode());
    }

    @Test
    void statusChangeIsPersistedAndBlockedAirportsRemainReadable() {
        assertEquals(AirportStatus.BLOCKED, service.updateStatus(airport.getId(), AirportStatus.BLOCKED).status());
        assertEquals(AirportStatus.BLOCKED, service.getAirportById(airport.getId()).status());
        verify(repository).save(airport);
    }
}
