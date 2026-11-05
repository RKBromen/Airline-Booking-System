package com.airline.airline_booking_system.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.airline.airline_booking_system.payment.dto.PaymentDetailDTO;
import com.airline.airline_booking_system.payment.dto.PaymentHistoryDTO;
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

        @Query(value = """
                        select id, payment_method, amount, status, created_at, paid_at
                        from payments where booking_id = :bookingId;
                        """, nativeQuery = true)
        List<PaymentHistoryDTO> findPaymentByBookingId(@Param("bookingId") String bookingId);

        @Query(value = """
                        select p.id, p.booking_id, b.booking_code, p.amount, p.payment_method,
                        p.status, p.transaction_id, p.created_at, p.paid_at, b.updated_at
                        from payments p, bookings b where p.booking_id = b.id and p.id = :paymentId;
                                                """, nativeQuery = true)
        PaymentDetailDTO findPaymentDetailDTO(@Param("paymentId") String paymentId);
}
