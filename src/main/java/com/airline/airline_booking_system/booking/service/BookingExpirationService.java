package com.airline.airline_booking_system.booking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.airline.airline_booking_system.booking.entity.Bookings;
import com.airline.airline_booking_system.booking.repository.BookingRepository;

@Service
public class BookingExpirationService {
    private static final Logger log = LoggerFactory.getLogger(BookingExpirationService.class);

    private final BookingRepository bookingRepository;
    private final BookingExpirationProcessor expirationProcessor;

    public BookingExpirationService(BookingRepository bookingRepository,
            BookingExpirationProcessor expirationProcessor) {
        this.bookingRepository = bookingRepository;
        this.expirationProcessor = expirationProcessor;
    }

    public void processExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();

        List<String> bookingIds = bookingRepository.findExpiredBookingIds(
                Bookings.BookingStatus.PENDING, now, PageRequest.of(0, 100));

        for (String bookingId : bookingIds) {
            try {
                expirationProcessor.expireBooking(bookingId);
            } catch (Exception e) {
                log.error(
                        "Failed to expire booking id={}. Will retry in a future run.",
                        bookingId,
                        e);
            }
        }
    }
}
