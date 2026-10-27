package com.airline.airline_booking_system.booking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.airline.airline_booking_system.booking.entity.Bookings;
import com.airline.airline_booking_system.booking.repository.BookingRepository;
import com.airline.airline_booking_system.flightschedule.entity.FlightSeats;
import com.airline.airline_booking_system.flightschedule.repository.FlightSeatRepository;
import com.airline.airline_booking_system.payment.entity.Payments;
import com.airline.airline_booking_system.payment.repository.PaymentRepository;

import jakarta.transaction.Transactional;

@Service
public class BookingExpirationProcessor {
    private final BookingRepository bookingRepository;
    private final FlightSeatRepository flightSeatRepository;
    private final PaymentRepository paymentRepository;

    public BookingExpirationProcessor(BookingRepository bookingRepository, FlightSeatRepository flightSeatRepository,
            PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.flightSeatRepository = flightSeatRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void expireBooking(String bookingId) {
        Bookings booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElse(null);

        if (booking == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (booking.getStatus() != Bookings.BookingStatus.PENDING
                || booking.getExpires_at() == null
                || booking.getExpires_at().isAfter(now)) {
            return;
        }

        boolean hasSuccessfulPayment = paymentRepository.existsByBookingIdAndStatus(
                bookingId, Payments.PaymentStatus.SUCCESS);

        if (hasSuccessfulPayment) {
            return;
        }

        boolean hasPendingPayment = paymentRepository.existsByBookingIdAndStatus(
                bookingId, Payments.PaymentStatus.PENDING);

        if (hasPendingPayment) {
            return;
        }

        List<FlightSeats> heldSeats = flightSeatRepository.findHeldSeatsByBookingIdForUpdate(
                bookingId);

        for (FlightSeats seat : heldSeats) {
            seat.setStatus(FlightSeats.FlightSeatsStatus.AVAILABLE);
            seat.setBookings(null);
            seat.setHeld_until(null);
        }

        booking.setStatus(Bookings.BookingStatus.EXPIRED);
        booking.setUpdated_at(now);

        flightSeatRepository.saveAll(heldSeats);
        bookingRepository.save(booking);
    }
}
