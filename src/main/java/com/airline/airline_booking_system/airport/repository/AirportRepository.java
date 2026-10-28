package com.airline.airline_booking_system.airport.repository;

import com.airline.airline_booking_system.airport.entity.Airports;
import com.airline.airline_booking_system.airport.enums.AirportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AirportRepository extends JpaRepository<Airports, UUID> {
    List<Airports> findByStatus(AirportStatus status);

    Optional<Airports> findByIdAndStatus(UUID id, AirportStatus status);

    boolean existsByCode(String code);

    boolean existsByCityAndCountry(String city, String country);

    boolean existsByName(String name);
}
