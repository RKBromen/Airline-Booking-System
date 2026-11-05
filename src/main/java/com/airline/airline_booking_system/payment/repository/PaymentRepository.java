package com.airline.airline_booking_system.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.airline.airline_booking_system.payment.entity.Payments;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, String> {
    @Query("""
                SELECT COUNT(p) > 0
                FROM Payments p
                WHERE p.bookings.id = :bookingId
                  AND p.status = :status
            """)
    boolean existsByBookingIdAndStatus(
            @Param("bookingId") String bookingId,
            @Param("status") Payments.PaymentStatus status);
}
