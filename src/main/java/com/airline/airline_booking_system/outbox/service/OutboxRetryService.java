package com.airline.airline_booking_system.outbox.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.outbox.entity.OutboxEvent;
import com.airline.airline_booking_system.outbox.enums.OutboxStatus;
import com.airline.airline_booking_system.outbox.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxRetryService {

    private static final int MAX_RETRIES = 5;

    private static final long INITIAL_DELAY = 5;

    private static final long MAX_DELAY = 300;

    private final OutboxEventRepository repository;

    @Transactional
    public void handleFailure(
            OutboxEvent event,
            Exception exception) {

        event.setRetryCount(event.getRetryCount() + 1);

        event.setLastError(exception.getMessage());

        event.setLockedUntil(null);

        if (event.getRetryCount() >= MAX_RETRIES) {

            event.setStatus(OutboxStatus.FAILED);

        } else {

            event.setStatus(OutboxStatus.PENDING);

            long delay = calculateRetryDelay(
                    event.getRetryCount());

            event.setNextRetryAt(
                    LocalDateTime.now().plusSeconds(delay));
        }

        repository.save(event);
    }

    private long calculateRetryDelay(int retryCount) {

        return Math.min(
                INITIAL_DELAY * (1L << Math.max(0, retryCount - 1)),
                MAX_DELAY);
    }
}
