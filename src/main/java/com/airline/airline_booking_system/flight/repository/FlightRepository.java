package com.airline.airline_booking_system.flight.repository;

import java.time.LocalDate;
import java.util.List;
import com.airline.airline_booking_system.flight.dto.FlightDTO;
import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleDetail;
import com.airline.airline_booking_system.flightschedule.dto.FlightSeatResponse;
import com.airline.airline_booking_system.flightschedule.dto.SearchFlight;
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

        @Query(value = """
                        SELECT fs.id, f.flight_number, dep.code, arr.code, fs.departure_time, fs.arrival_time, fs.base_price, count(fss.id) as availableSeats
                        FROM flights f, flight_schedules fs, flight_seats fss, airports dep, airports arr
                        WHERE f.id = fs.flight_id AND fs.id = fss.flight_schedule_id AND dep.id = f.departure_airport_id AND
                        arr.id = f.arrival_airport_id AND f.status = 'ACTIVE' AND fs.status = 'SCHEDULED'
                        AND fss.status = 'AVAILABLE' AND DATE(fs.departure_time) = :date AND
                        dep.code = :departure AND arr.code = :arrival
                        group by fs.id, f.id
                        ORDER BY fs.departure_time
                        """, nativeQuery = true)
        List<SearchFlight> searchFlight(@Param("departure") String departure, @Param("arrival") String arrival,
                        @Param("date") LocalDate date);

        @Query(value = """
                        select ais.id, ais.seat_number, ais.seat_class, fs.price, fs.status from flight_schedules fsc, flight_seats fs, aircraft_seats ais
                        where fsc.id = fs.flight_schedule_id and fs.aircraft_seat_id = ais.id and fsc.id = :schedule_id
                                                """, nativeQuery = true)
        List<FlightSeatResponse> seatingChart(@Param("schedule_id") String schedule_id);

        @Query(value = """
                        select fsc.id, f.flight_number, dep.city, dep.country, arr.city, arr.country, fsc.departure_time, fsc.arrival_time,
                        timediff(fsc.arrival_time, fsc.departure_time) as duration, ac.registration_no from
                        flights f, flight_schedules fsc, aircrafts ac, airports dep, airports arr
                        where f.id = fsc.flight_id and ac.id = fsc.aircraft_id and dep.id = f.departure_airport_id
                        and arr.id = f.arrival_airport_id and fsc.id = :schedule_id
                                                """, nativeQuery = true)
        FlightScheduleDetail getScheduleById(@Param("schedule_id") String schedule_id);

        @Query(value = """
                                select f.flight_number, dep.code, arr.code, fsc.departure_time
                                from flights f, flight_schedules fsc, airports dep, airports arr, bookings b
                                where f.id = fsc.flight_id and f.departure_airport_id = dep.id and fsc.id = b.flight_schedule_id
                                and f.arrival_airport_id = arr.id and b.id = :bookingId;
                        """, nativeQuery = true)
        FlightDTO getFlight(@Param("bookingId") String bookingId);
}
