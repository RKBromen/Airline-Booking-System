package com.airline.airline_booking_system.payment.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.booking.entity.Bookings;
import com.airline.airline_booking_system.booking.repository.BookingRepository;
import com.airline.airline_booking_system.payment.dto.PaymentCreateRequest;
import com.airline.airline_booking_system.payment.dto.PaymentResponse;
import com.airline.airline_booking_system.payment.entity.Payments;
import com.airline.airline_booking_system.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentConfirmationService {
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPendingPayments(String bookingId, String userId, PaymentCreateRequest request) {
        Bookings booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUsers().getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to pay for this booking");
        }

        if (booking.getStatus() != Bookings.BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not pending");
        }

        LocalDateTime now = LocalDateTime.now();

        if (booking.getExpires_at() == null || !booking.getExpires_at().isAfter(now)) {
            throw new IllegalStateException("Booking payment has expired");
        }

        if (paymentRepository.existsByBookingIdAndStatus(bookingId, Payments.PaymentStatus.PENDING)) {
            throw new IllegalStateException("A pending payment already exists");
        }

        Payments payments = new Payments(UUID.randomUUID().toString(), booking,
                Payments.PaymentMethod.valueOf(request.paymentMethod()),
                booking.getTotal_amount(), Payments.PaymentStatus.PENDING, UUID.randomUUID().toString(), now, null);

        paymentRepository.save(payments);

        return new PaymentResponse(payments.getId(), bookingId, payments.getAmount(), request.paymentMethod(),
                Payments.PaymentStatus.PENDING.toString(), null);
    }
}
