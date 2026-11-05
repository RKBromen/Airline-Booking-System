package com.airline.airline_booking_system.aircraft;

import com.airline.airline_booking_system.aircraft.dto.AircraftRequest;
import com.airline.airline_booking_system.aircraft.dto.AircraftSeatRequest;
import com.airline.airline_booking_system.aircraft.entity.AircraftSeats;
import com.airline.airline_booking_system.aircraft.entity.Aircrafts;
import com.airline.airline_booking_system.aircraft.enums.AircraftStatus;
import com.airline.airline_booking_system.aircraft.enums.SeatClass;
import com.airline.airline_booking_system.aircraft.repository.AircraftRepository;
import com.airline.airline_booking_system.aircraft.repository.AircraftSeatRepository;
import com.airline.airline_booking_system.aircraft.service.AircraftService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Runs against the configured MySQL database; every test rolls back its fixtures. */
@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
@Transactional
class AircraftPersistenceIntegrationTest {
    @Autowired AircraftService service;
    @Autowired AircraftRepository aircrafts;
    @Autowired AircraftSeatRepository seats;
    @Autowired EntityManager entityManager;

    @Test
    void aircraftAndSeatPersistWithDatabaseDefaultsAndCorrectRelationship() {
        var aircraft = service.createAircraft(new AircraftRequest(registration()));
        UUID id = UUID.fromString(aircraft.id());
        var seat = service.addSeat(id, new AircraftSeatRequest("12a", SeatClass.ECONOMY));
        entityManager.clear();
        Aircrafts stored = aircrafts.findById(aircraft.id()).orElseThrow();
        assertNotNull(stored.getCreated_at());
        assertNotNull(stored.getUpdated_at());
        assertEquals(AircraftStatus.ACTIVE, stored.getStatus());
        AircraftSeats storedSeat = seats.findById(seat.id()).orElseThrow();
        assertEquals(aircraft.id(), storedSeat.getAircrafts().getId());
        assertEquals("12A", storedSeat.getSeatNumber());
        assertEquals(SeatClass.ECONOMY, storedSeat.getSeatClass());
    }

    @Test
    void sameSeatNumberCanExistOnDifferentAircraftAndListsStayScoped() {
        UUID first = UUID.fromString(service.createAircraft(new AircraftRequest(registration())).id());
        UUID second = UUID.fromString(service.createAircraft(new AircraftRequest(registration())).id());
        service.addSeat(first, new AircraftSeatRequest("1A", SeatClass.BUSINESS));
        service.addSeat(second, new AircraftSeatRequest("1A", SeatClass.FIRST));
        entityManager.clear();
        assertEquals(1, service.listSeats(first).size());
        assertEquals(SeatClass.BUSINESS, service.listSeats(first).getFirst().seatClass());
        assertEquals(1, service.listSeats(second).size());
        assertEquals(SeatClass.FIRST, service.listSeats(second).getFirst().seatClass());
    }

    @Test
    void databaseRejectsDuplicateSeatWithinOneAircraft() {
        UUID id = UUID.fromString(service.createAircraft(new AircraftRequest(registration())).id());
        service.addSeat(id, new AircraftSeatRequest("12A", SeatClass.ECONOMY));
        Aircrafts parent = aircrafts.findById(id.toString()).orElseThrow();
        AircraftSeats duplicate = new AircraftSeats(UUID.randomUUID().toString(), parent, "12A", SeatClass.FIRST);
        assertThrows(DataIntegrityViolationException.class, () -> seats.saveAndFlush(duplicate));
    }

    @Test
    void databaseRejectsDuplicateRegistration() {
        String registration = registration();
        service.createAircraft(new AircraftRequest(registration));
        Aircrafts duplicate = new Aircrafts();
        duplicate.setId(UUID.randomUUID().toString());
        duplicate.setRegistrationNo(registration);
        duplicate.setStatus(AircraftStatus.ACTIVE);
        assertThrows(DataIntegrityViolationException.class, () -> aircrafts.saveAndFlush(duplicate));
    }

    @Test
    void updatingRegistrationAndStatusKeepsPhysicalSeatLayout() {
        UUID id = UUID.fromString(service.createAircraft(new AircraftRequest(registration())).id());
        var seat = service.addSeat(id, new AircraftSeatRequest("12A", SeatClass.ECONOMY));
        String updatedRegistration = registration();
        service.updateAircraft(id, new AircraftRequest(updatedRegistration));
        service.updateStatus(id, AircraftStatus.BLOCKED);
        entityManager.flush();
        entityManager.clear();
        assertEquals(updatedRegistration, service.getAircraft(id).registrationNo());
        assertEquals(AircraftStatus.BLOCKED, service.getAircraft(id).status());
        assertEquals(seat.id(), service.listSeats(id).getFirst().id());
    }

    private String registration() {
        return "TEST-" + UUID.randomUUID().toString().toUpperCase(java.util.Locale.ROOT);
    }
}
