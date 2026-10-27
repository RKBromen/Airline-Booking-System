package com.airline.airline_booking_system.booking.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.airline.airline_booking_system.booking.service.BookingExpirationService;

@Component
public class BookingExpirationJob {
    private static final Logger log = LoggerFactory.getLogger(BookingExpirationJob.class);

    private final BookingExpirationService expirationService;

    public BookingExpirationJob(
            BookingExpirationService expirationService) {
        this.expirationService = expirationService;
    }

    @Scheduled(fixedDelay = 30000)
    public void expireBookings() {
        log.info("Starting booking expiration job");

        expirationService.processExpiredBookings();

        log.info("Finished booking expiration job");
    }
}
