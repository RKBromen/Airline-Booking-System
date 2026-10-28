package com.airline.airline_booking_system.aircraft.service;

import com.airline.airline_booking_system.aircraft.dto.*;
import com.airline.airline_booking_system.aircraft.entity.AircraftSeats;
import com.airline.airline_booking_system.aircraft.entity.Aircrafts;
import com.airline.airline_booking_system.aircraft.enums.AircraftStatus;
import com.airline.airline_booking_system.aircraft.repository.AircraftRepository;
import com.airline.airline_booking_system.aircraft.repository.AircraftSeatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AircraftService {
    private final AircraftRepository aircraftRepository;
    private final AircraftSeatRepository seatRepository;

    public List<AircraftResponse> listAircrafts() {
        return aircraftRepository.findAll(Sort.by("registrationNo")).stream().map(this::toResponse).toList();
    }

    public AircraftResponse getAircraft(UUID id) {
        return toResponse(findAircraft(id));
    }

    @Transactional
    public AircraftResponse createAircraft(AircraftRequest request) {
        String registration = normalize(request.registrationNo());
        if (aircraftRepository.existsByRegistrationNoIgnoreCase(registration)) {
            throw duplicateRegistration(registration);
        }
        Aircrafts aircraft = new Aircrafts();
        aircraft.setId(UUID.randomUUID().toString());
        aircraft.setRegistrationNo(registration);
        aircraft.setStatus(AircraftStatus.ACTIVE);
        return toResponse(persistUnique(() -> aircraftRepository.saveAndFlush(aircraft),
                "uk_aircrafts_registration", () -> duplicateRegistration(registration)));
    }

    @Transactional
    public AircraftResponse updateAircraft(UUID id, AircraftRequest request) {
        Aircrafts aircraft = findAircraft(id);
        String registration = normalize(request.registrationNo());
        if (aircraftRepository.existsByRegistrationNoIgnoreCaseAndIdNot(registration, aircraft.getId())) {
            throw duplicateRegistration(registration);
        }
        aircraft.setRegistrationNo(registration);
        return toResponse(persistUnique(() -> aircraftRepository.saveAndFlush(aircraft),
                "uk_aircrafts_registration", () -> duplicateRegistration(registration)));
    }

    @Transactional
    public AircraftResponse updateStatus(UUID id, AircraftStatus status) {
        Aircrafts aircraft = findAircraft(id);
        aircraft.setStatus(status);
        return toResponse(aircraftRepository.save(aircraft));
    }

    public List<AircraftSeatListItem> listSeats(UUID id) {
        Aircrafts aircraft = findAircraft(id);
        return seatRepository.findByAircraftsIdOrderBySeatNumberAsc(aircraft.getId()).stream()
                .map(seat -> new AircraftSeatListItem(seat.getId(), seat.getSeatNumber(), seat.getSeatClass()))
                .toList();
    }

    @Transactional
    public AircraftSeatResponse addSeat(UUID id, AircraftSeatRequest request) {
        Aircrafts aircraft = findAircraft(id);
        String number = normalize(request.seatNumber());
        if (seatRepository.existsByAircraftsIdAndSeatNumberIgnoreCase(aircraft.getId(), number)) {
            throw duplicateSeat(number);
        }
        AircraftSeats seat = new AircraftSeats(UUID.randomUUID().toString(), aircraft, number, request.seatClass());
        AircraftSeats saved = persistUnique(() -> seatRepository.saveAndFlush(seat),
                "uk_aircraft_seat", () -> duplicateSeat(number));
        return new AircraftSeatResponse(saved.getId(), saved.getAircrafts().getId(), saved.getSeatNumber(), saved.getSeatClass());
    }

    private Aircrafts findAircraft(UUID id) {
        return aircraftRepository.findById(id.toString())
                .orElseThrow(() -> new EntityNotFoundException("Aircraft not found with id: " + id));
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private AircraftResponse toResponse(Aircrafts aircraft) {
        return new AircraftResponse(aircraft.getId(), aircraft.getRegistrationNo(), aircraft.getStatus());
    }

    private AircraftConflictException duplicateRegistration(String registration) {
        return new AircraftConflictException("Aircraft registration already exists: " + registration);
    }

    private AircraftConflictException duplicateSeat(String number) {
        return new AircraftConflictException("Seat number already exists on this aircraft: " + number);
    }

    private <T> T persistUnique(Supplier<T> save, String constraint, Supplier<AircraftConflictException> conflict) {
        try {
            return save.get();
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation && violation.getConstraintName() != null) {
                    String actual = violation.getConstraintName().toLowerCase(Locale.ROOT);
                    if (actual.equals(constraint) || actual.endsWith("." + constraint)) {
                        throw conflict.get();
                    }
                }
            }
            throw exception;
        }
    }
}
