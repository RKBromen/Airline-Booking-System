package com.airline.airline_booking_system.airport.service;

import com.airline.airline_booking_system.airport.dtos.request.AirportRequest;
import com.airline.airline_booking_system.airport.dtos.request.UpdateAirportRequest;
import com.airline.airline_booking_system.airport.dtos.response.AirportResponse;
import com.airline.airline_booking_system.airport.entity.Airports;
import com.airline.airline_booking_system.airport.enums.AirportStatus;
import com.airline.airline_booking_system.airport.repository.AirportRepository;
import com.airline.airline_booking_system.common.exceptions.AirportCodeAlreadyExistException;
import com.airline.airline_booking_system.common.exceptions.AirportNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirportService {
    private final AirportRepository airportRepository;

    public List<AirportResponse> getAirports(String keyword, AirportStatus status) {
        Specification<Airports> filter = (root, query, cb) -> cb.conjunction();
        if (status != null) {
            filter = filter.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            filter = filter.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), pattern, '\\'),
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("city")), pattern, '\\'),
                    cb.like(cb.lower(root.get("country")), pattern, '\\')));
        }
        return airportRepository.findAll(filter, Sort.by("code")).stream().map(this::toResponse).toList();
    }

    public AirportResponse getAirportById(UUID id) {
        return toResponse(findAirport(id));
    }

    @Transactional
    public AirportResponse createAirport(AirportRequest request) {
        String code = request.code().toUpperCase(Locale.ROOT);
        if (airportRepository.existsByCodeIgnoreCase(code)) {
            throw new AirportCodeAlreadyExistException(code);
        }
        Airports airport = new Airports();
        airport.setId(UUID.randomUUID());
        airport.setCode(code);
        airport.setName(request.name().trim());
        airport.setCity(request.city().trim());
        airport.setCountry(request.country().trim());
        airport.setStatus(AirportStatus.ACTIVE);
        try {
            return toResponse(airportRepository.saveAndFlush(airport));
        } catch (DataIntegrityViolationException exception) {
            // The database unique constraint also protects concurrent creates.
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                    String constraint = violation.getConstraintName();
                    if (constraint != null && (constraint.equalsIgnoreCase("uk_airports_code")
                            || constraint.toLowerCase(Locale.ROOT).endsWith(".uk_airports_code"))) {
                        throw new AirportCodeAlreadyExistException(code);
                    }
                }
            }
            throw exception;
        }
    }

    @Transactional
    public AirportResponse updateAirport(UUID id, UpdateAirportRequest request, boolean partial) {
        Airports airport = findAirport(id);
        if (request.code() != null && !airport.getCode().equalsIgnoreCase(request.code())) {
            throw new IllegalArgumentException("Airport code cannot be changed after creation");
        }
        if (!partial && (request.name() == null || request.city() == null || request.country() == null)) {
            throw new IllegalArgumentException("Airport name, city and country are required for PUT");
        }
        if (request.name() != null) airport.setName(request.name().trim());
        if (request.city() != null) airport.setCity(request.city().trim());
        if (request.country() != null) airport.setCountry(request.country().trim());
        return toResponse(airportRepository.save(airport));
    }

    @Transactional
    public AirportResponse updateStatus(UUID id, AirportStatus status) {
        Airports airport = findAirport(id);
        airport.setStatus(status);
        return toResponse(airportRepository.save(airport));
    }

    private Airports findAirport(UUID id) {
        return airportRepository.findById(id).orElseThrow(() -> new AirportNotFoundException(id));
    }

    private AirportResponse toResponse(Airports airport) {
        return new AirportResponse(airport.getId(), airport.getCode(), airport.getName(),
                airport.getCity(), airport.getCountry(), airport.getStatus());
    }
}
