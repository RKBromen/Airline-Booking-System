package com.airline.airline_booking_system.flightschedule.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.airline.airline_booking_system.flightschedule.entity.FlightSeats;

import jakarta.persistence.LockModeType;

@Repository
public interface FlightSeatRepository extends JpaRepository<FlightSeats, String> {
  @Modifying(flushAutomatically = true)
  @Query(value = """
      update flight_seats set status = 'HELD', held_by_booking_id = :bookingId,
      held_until = :date where id = :seatId and flight_schedule_id = :flightScheduleId
      and (status = 'AVAILABLE' or (status = 'HELD' and held_until <= :now))
      """, nativeQuery = true)
  int holdSeat(@Param("bookingId") String booking_id, @Param("date") LocalDateTime date,
      @Param("seatId") String seat_id,
      @Param("flightScheduleId") String flight_schedule_id, @Param("now") LocalDateTime now);

  @Modifying
  @Query(value = """
      UPDATE flight_seats
      SET status = 'AVAILABLE',
          held_by_booking_id = NULL,
          held_until = NULL
      WHERE held_by_booking_id = :bookingId
        AND status = 'HELD'
      """, nativeQuery = true)
  int releaseHeldSeats(@Param("bookingId") String bookingId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
          SELECT s
          FROM FlightSeats s
          WHERE s.bookings.id = :bookingId
            AND s.status = com.airline.airline_booking_system.flightschedule.entity.FlightSeats.FlightSeatsStatus.HELD
      """)
  List<FlightSeats> findHeldSeatsByBookingIdForUpdate(
      @Param("bookingId") String bookingId);

}
