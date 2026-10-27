package com.airline.airline_booking_system.booking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.airline.airline_booking_system.booking.dto.BookingDTO;
import com.airline.airline_booking_system.booking.dto.BookingHistory;
import com.airline.airline_booking_system.booking.entity.Bookings;

import jakarta.persistence.LockModeType;

@Repository
public interface BookingRepository extends JpaRepository<Bookings, String> {
  boolean existsByBookingCode(String bookingCode);

  @Query(value = """
      select b.id, b.booking_code, b.created_at, b.status, b.total_amount, f.flight_number,
      dep.code, arr.code, fsc.departure_time, fsc.arrival_time
      from bookings b, flight_schedules fsc, flights f, airports dep, airports arr
      where b.flight_schedule_id = fsc.id and fsc.flight_id = f.id
      and dep.id = f.departure_airport_id and arr.id = f.arrival_airport_id
      and b.user_id = :userId;
                  """, nativeQuery = true)
  List<BookingHistory> history(@Param("userId") String userId);

  @Query(value = """
      select * from bookings where booking_code = :code
      """, nativeQuery = true)
  Bookings findByBookingCode(@Param("code") String bookingCode);

  @Query(value = """
      select b.id, b.booking_code, b.created_at, b.status, bp.full_name, f.flight_number, dep.code, arr.code, fsc.departure_time, b.total_amount
      from bookings b, booking_passengers bp, flights f, flight_schedules fsc, airports dep, airports arr
      where b.id = bp.booking_id and b.flight_schedule_id = fsc.id and fsc.flight_id = f.id
      and f.departure_airport_id = dep.id and f.arrival_airport_id = arr.id
                  """, nativeQuery = true)
  List<BookingDTO> getAll();

  @Query("""
            SELECT b.id
      FROM Bookings b
      WHERE b.status = :status
        AND b.expires_at IS NOT NULL
        AND b.expires_at <= :now
        AND NOT EXISTS (
            SELECT p.id
            FROM Payments p
            WHERE p.bookings.id = b.id
              AND p.status IN (
                  com.airline.airline_booking_system.payment.entity.Payments.PaymentStatus.PENDING,
                  com.airline.airline_booking_system.payment.entity.Payments.PaymentStatus.SUCCESS
              )
        )
      ORDER BY b.expires_at ASC
        """)
  List<String> findExpiredBookingIds(
      @Param("status") Bookings.BookingStatus status,
      @Param("now") LocalDateTime now, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT b FROM Bookings b WHERE b.id = :id")
  Optional<Bookings> findByIdForUpdate(@Param("id") String id);
}
