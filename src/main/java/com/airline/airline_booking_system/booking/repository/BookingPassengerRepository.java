package com.airline.airline_booking_system.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.airline.airline_booking_system.booking.dto.BookingPassengerDTO;
import com.airline.airline_booking_system.booking.entity.BookingPassengers;

@Repository
public interface BookingPassengerRepository extends JpaRepository<BookingPassengers, String> {
    @Query(value = """
            select bp.id, bp.full_name, ais.seat_number from booking_passengers bp, flight_seats fs, aircraft_seats ais
            where bp.flight_seat_id = fs.id and fs.aircraft_seat_id = ais.id and booking_id = :bookingId;
            """, nativeQuery = true)
    List<BookingPassengerDTO> getPassenger(@Param("bookingId") String bookingId);
}
