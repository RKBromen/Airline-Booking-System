package com.airline.airline_booking_system.payment.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.booking.entity.Bookings;
import com.airline.airline_booking_system.booking.repository.BookingRepository;
import com.airline.airline_booking_system.payment.dto.PaymentCreateRequest;
import com.airline.airline_booking_system.payment.dto.PendingPaymentData;
import com.airline.airline_booking_system.payment.entity.Payments;
import com.airline.airline_booking_system.payment.entity.Payments.PaymentMethod;
import com.airline.airline_booking_system.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentConfirmationService {
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PendingPaymentData createPendingPayments(String bookingId, String userId, PaymentCreateRequest request) {
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

        Payments.PaymentMethod requestedMethod = PaymentMethod.valueOf(request.paymentMethod());

        var existingPayment = paymentRepository
                .findByBookings_IdAndStatus(
                        bookingId,
                        Payments.PaymentStatus.PENDING);

        if (existingPayment.isPresent()) {
            Payments payment = existingPayment.get();

            if (payment.getPayment_method() != requestedMethod) {
                throw new IllegalStateException(
                        "A pending payment already exists with another payment method");
            }

            return toPendingPaymentData(payment);
        }

        boolean alreadyPaid = paymentRepository
                .existsByBookings_IdAndStatus(
                        bookingId,
                        Payments.PaymentStatus.SUCCESS);

        if (alreadyPaid) {
            throw new IllegalStateException(
                    "Booking has already been paid");
        }

        Payments payments = new Payments(UUID.randomUUID().toString().replace("-", ""), booking,
                Payments.PaymentMethod.valueOf(request.paymentMethod()),
                booking.getTotal_amount(), Payments.PaymentStatus.PENDING, UUID.randomUUID().toString(), now, null,
                null);

        paymentRepository.save(payments);

        return toPendingPaymentData(payments);
    }

    @Transactional
    public void savePaymentUrl(
            String paymentId,
            String paymentUrl) {

        Payments payment = paymentRepository
                .findByIdForUpdate(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != Payments.PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Payment is no longer pending");
        }

        payment.setPayment_url(paymentUrl);

        paymentRepository.save(payment);
    }

    private PendingPaymentData toPendingPaymentData(Payments payment) {
        return new PendingPaymentData(payment.getId(), payment.getBookings().getId(), payment.getAmount(),
                payment.getPayment_method().name(), payment.getStatus().name(), payment.getTransactionId(),
                payment.getPayment_url());
    }
}
