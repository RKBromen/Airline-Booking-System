package com.airline.airline_booking_system.flight.repository;

import java.util.Optional;
import java.util.UUID;

import com.airline.airline_booking_system.flight.entity.Flights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightRepository extends JpaRepository<Flights, String>, JpaSpecificationExecutor<Flights> {
    @Query("""
            select f.id from Flights f
            where lower(f.flight_number) = lower(:flightNumber)
              and (:excludedId is null or f.id <> :excludedId)
            """)
    Optional<String> findFlightByNumber(@Param("flightNumber") String flightNumber,
            @Param("excludedId") String excludedId);

    @Query("""
            select f.id from Flights f
            where f.departure_airport_id.id = :departureAirportId
              and f.arrival_airport_id.id = :arrivalAirportId
              and (:excludedId is null or f.id <> :excludedId)
            """)
    Optional<String> findFlightOnRoute(@Param("departureAirportId") UUID departureAirportId,
            @Param("arrivalAirportId") UUID arrivalAirportId, @Param("excludedId") String excludedId);
}
