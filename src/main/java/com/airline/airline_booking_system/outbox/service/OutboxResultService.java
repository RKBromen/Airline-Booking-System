
package com.airline.airline_booking_system.outbox.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.outbox.entity.OutboxEvent;
import com.airline.airline_booking_system.outbox.enums.OutboxStatus;
import com.airline.airline_booking_system.outbox.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxResultService {

    private static final int MAX_RETRIES = 10;
    private static final long INITIAL_RETRY_DELAY_SECONDS = 5;
    private static final long MAX_RETRY_DELAY_SECONDS = 300;

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void markPublished(UUID eventId) {
        OutboxEvent event = findEvent(eventId);

        if (event.getStatus() != OutboxStatus.PROCESSING) {
            return;
        }

        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        event.setLockedUntil(null);
        event.setLastError(null);
    }

    @Transactional
    public void markFailed(UUID eventId, Exception exception) {
        OutboxEvent event = findEvent(eventId);

        if (event.getStatus() != OutboxStatus.PROCESSING) {
            return;
        }

        int retryCount = event.getRetryCount() + 1;

        event.setRetryCount(retryCount);
        event.setLockedUntil(null);
        event.setLastError(
                exception.getClass().getSimpleName()
                        + ": " + exception.getMessage());

        if (retryCount >= MAX_RETRIES) {
            event.setStatus(OutboxStatus.FAILED);
            return;
        }

        event.setStatus(OutboxStatus.PENDING);

        long delay = Math.min(
                INITIAL_RETRY_DELAY_SECONDS * (1L << (retryCount - 1)),
                MAX_RETRY_DELAY_SECONDS);

        event.setNextRetryAt(
                LocalDateTime.now().plusSeconds(delay));
    }

    private OutboxEvent findEvent(UUID eventId) {
        return outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox event not found: " + eventId));
    }
}
