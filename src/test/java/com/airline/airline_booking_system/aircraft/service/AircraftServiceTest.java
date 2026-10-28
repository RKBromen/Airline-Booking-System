package com.airline.airline_booking_system.aircraft.service;

import com.airline.airline_booking_system.aircraft.dto.AircraftRequest;
import com.airline.airline_booking_system.aircraft.dto.AircraftSeatRequest;
import com.airline.airline_booking_system.aircraft.entity.AircraftSeats;
import com.airline.airline_booking_system.aircraft.entity.Aircrafts;
import com.airline.airline_booking_system.aircraft.enums.AircraftStatus;
import com.airline.airline_booking_system.aircraft.enums.SeatClass;
import com.airline.airline_booking_system.aircraft.repository.AircraftRepository;
import com.airline.airline_booking_system.aircraft.repository.AircraftSeatRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AircraftServiceTest {
    private AircraftRepository aircrafts;
    private AircraftSeatRepository seats;
    private AircraftService service;
    private Aircrafts aircraft;
    private UUID id;

    @BeforeEach
    void setUp() {
        aircrafts = mock(AircraftRepository.class);
        seats = mock(AircraftSeatRepository.class);
        service = new AircraftService(aircrafts, seats);
        id = UUID.randomUUID();
        aircraft = new Aircrafts();
        aircraft.setId(id.toString());
        aircraft.setRegistrationNo("VN-A321");
        aircraft.setStatus(AircraftStatus.ACTIVE);
        when(aircrafts.findById(id.toString())).thenReturn(Optional.of(aircraft));
        when(aircrafts.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        when(aircrafts.save(any())).thenAnswer(call -> call.getArgument(0));
        when(seats.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void creationNormalizesRegistrationAndAssignsUuidAndActiveStatus() {
        var response = service.createAircraft(new AircraftRequest(" vn-a321 "));
        assertNotNull(UUID.fromString(response.id()));
        assertEquals("VN-A321", response.registrationNo());
        assertEquals(AircraftStatus.ACTIVE, response.status());
        verify(aircrafts).existsByRegistrationNoIgnoreCase("VN-A321");
    }

    @Test
    void duplicateRegistrationIsRejectedOnCreationAndUpdate() {
        when(aircrafts.existsByRegistrationNoIgnoreCase("VN-A321")).thenReturn(true);
        when(aircrafts.existsByRegistrationNoIgnoreCaseAndIdNot("VN-A999", id.toString())).thenReturn(true);
        assertThrows(AircraftConflictException.class, () -> service.createAircraft(new AircraftRequest("vn-a321")));
        assertThrows(AircraftConflictException.class, () -> service.updateAircraft(id, new AircraftRequest("VN-A999")));
        verify(aircrafts, never()).saveAndFlush(any());
        assertEquals("VN-A321", aircraft.getRegistrationNo());
    }

    @Test
    void updatePreservesIdStatusAndPhysicalSeats() {
        aircraft.setStatus(AircraftStatus.BLOCKED);
        AircraftSeats seat = new AircraftSeats("seat-id", aircraft, "12A", SeatClass.ECONOMY);
        aircraft.setSeats(List.of(seat));
        var response = service.updateAircraft(id, new AircraftRequest(" vn-a322 "));
        assertEquals(id.toString(), response.id());
        assertEquals("VN-A322", response.registrationNo());
        assertEquals(AircraftStatus.BLOCKED, response.status());
        assertSame(seat, aircraft.getSeats().getFirst());
    }

    @Test
    void statusChangesDoNotRemoveSeatsOrAircraft() {
        assertEquals(AircraftStatus.DELETED, service.updateStatus(id, AircraftStatus.DELETED).status());
        verify(aircrafts).save(aircraft);
        verify(aircrafts, never()).delete(any());
        verifyNoInteractions(seats);
    }

    @Test
    void seatCreationUsesParentAndNormalizesNumber() {
        var response = service.addSeat(id, new AircraftSeatRequest(" 12a ", SeatClass.ECONOMY));
        assertNotNull(UUID.fromString(response.id()));
        assertEquals(id.toString(), response.aircraftId());
        assertEquals("12A", response.seatNumber());
        assertEquals(SeatClass.ECONOMY, response.seatClass());
        verify(seats).existsByAircraftsIdAndSeatNumberIgnoreCase(id.toString(), "12A");
    }

    @Test
    void duplicateSeatOnSameAircraftIsRejected() {
        when(seats.existsByAircraftsIdAndSeatNumberIgnoreCase(id.toString(), "12A")).thenReturn(true);
        assertThrows(AircraftConflictException.class,
                () -> service.addSeat(id, new AircraftSeatRequest("12a", SeatClass.BUSINESS)));
        verify(seats, never()).saveAndFlush(any());
    }

    @Test
    void sameSeatNumberOnDifferentAircraftIsAllowed() {
        UUID otherId = UUID.randomUUID();
        Aircrafts other = new Aircrafts();
        other.setId(otherId.toString());
        when(aircrafts.findById(otherId.toString())).thenReturn(Optional.of(other));
        var first = service.addSeat(id, new AircraftSeatRequest("12A", SeatClass.ECONOMY));
        var second = service.addSeat(otherId, new AircraftSeatRequest("12A", SeatClass.FIRST));
        assertNotEquals(first.id(), second.id());
        assertNotEquals(first.aircraftId(), second.aircraftId());
        assertEquals(first.seatNumber(), second.seatNumber());
    }

    @Test
    void seatListIncludesOnlyRequestedAircraftAndMatchesResponseShape() {
        when(seats.findByAircraftsIdOrderBySeatNumberAsc(id.toString()))
                .thenReturn(List.of(new AircraftSeats("seat-id", aircraft, "1A", SeatClass.BUSINESS)));
        var response = service.listSeats(id);
        assertEquals(1, response.size());
        assertEquals("seat-id", response.getFirst().id());
        assertEquals("1A", response.getFirst().seatNumber());
        assertEquals(SeatClass.BUSINESS, response.getFirst().seatClass());
    }

    @Test
    void aircraftListMapsFieldsAndUsesRegistrationOrder() {
        when(aircrafts.findAll(Sort.by("registrationNo"))).thenReturn(List.of(aircraft));
        assertEquals("VN-A321", service.listAircrafts().getFirst().registrationNo());
        assertEquals(AircraftStatus.ACTIVE, service.getAircraft(id).status());
    }

    @Test
    void missingParentIsRejectedForEveryOperation() {
        UUID missing = UUID.randomUUID();
        assertThrows(EntityNotFoundException.class, () -> service.getAircraft(missing));
        assertThrows(EntityNotFoundException.class, () -> service.updateAircraft(missing, new AircraftRequest("VN-X")));
        assertThrows(EntityNotFoundException.class, () -> service.updateStatus(missing, AircraftStatus.BLOCKED));
        assertThrows(EntityNotFoundException.class, () -> service.listSeats(missing));
        assertThrows(EntityNotFoundException.class,
                () -> service.addSeat(missing, new AircraftSeatRequest("12A", SeatClass.ECONOMY)));
        verifyNoInteractions(seats);
    }

    @Test
    void concurrentDuplicatesAreTranslatedToConflicts() {
        when(aircrafts.saveAndFlush(any())).thenThrow(violation("aircrafts.uk_aircrafts_registration"));
        when(seats.saveAndFlush(any())).thenThrow(violation("uk_aircraft_seat"));
        assertThrows(AircraftConflictException.class, () -> service.createAircraft(new AircraftRequest("VN-A321")));
        assertThrows(AircraftConflictException.class, () -> service.updateAircraft(id, new AircraftRequest("VN-A322")));
        assertThrows(AircraftConflictException.class,
                () -> service.addSeat(id, new AircraftSeatRequest("12A", SeatClass.ECONOMY)));
    }

    @Test
    void unrelatedDatabaseFailuresAreNotReportedAsDuplicates() {
        var exception = violation("fk_aircraft_seats_aircrafts");
        when(seats.saveAndFlush(any())).thenThrow(exception);
        assertSame(exception, assertThrows(DataIntegrityViolationException.class,
                () -> service.addSeat(id, new AircraftSeatRequest("12A", SeatClass.ECONOMY))));
    }

    private DataIntegrityViolationException violation(String constraint) {
        return new DataIntegrityViolationException("Constraint violated",
                new ConstraintViolationException("Constraint violated", new SQLException("Constraint violated"), constraint));
    }
}
