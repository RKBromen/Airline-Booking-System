
package com.airline.airline_booking_system.outbox.job;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.airline.airline_booking_system.kafka.producer.OutboxKafkaPublisher;
import com.airline.airline_booking_system.outbox.entity.OutboxEvent;
import com.airline.airline_booking_system.outbox.service.OutboxClaimService;
import com.airline.airline_booking_system.outbox.service.OutboxResultService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherJob {

    private static final int BATCH_SIZE = 100;
    private static final int LOCK_DURATION_SECONDS = 180;

    private final OutboxClaimService outboxClaimService;
    private final OutboxKafkaPublisher outboxKafkaPublisher;
    private final OutboxResultService outboxResultService;

    @Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay}")
    public void publishPendingEvents() {

        log.info("Starting Outbox Publisher Job");

        var events = outboxClaimService.claimEvents(
                BATCH_SIZE,
                LOCK_DURATION_SECONDS);

        if (events.isEmpty()) {
            log.debug("No pending outbox events found");
            return;
        }

        log.info("Claimed {} outbox events", events.size());

        for (var event : events) {
            var eventId = event.getId();

            try {
                outboxKafkaPublisher.publish(event)
                        .whenComplete((result, exception) -> {
                            if (exception != null) {
                                handlePublishFailure(event, exception);
                                return;
                            }

                            try {
                                outboxResultService.markPublished(eventId);

                                log.info(
                                        "Published outbox event: id={}, topic={}, partition={}, offset={}",
                                        eventId,
                                        event.getTopic(),
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset());
                            } catch (Exception updateException) {
                                log.error(
                                        "Failed to update outbox result: id={}",
                                        eventId,
                                        updateException);
                            }
                        });

            } catch (Exception exception) {
                // Bắt lỗi đồng bộ khi khởi tạo Kafka Producer hoặc gọi send()
                handlePublishFailure(event, exception);
            }
        }

    }

    private void handlePublishFailure(OutboxEvent event, Throwable exception) {
        var eventId = event.getId();

        Throwable cause = exception;

        while ((cause instanceof CompletionException
                || cause instanceof ExecutionException)
                && cause.getCause() != null) {
            cause = cause.getCause();
        }

        Exception failure = cause instanceof Exception
                ? (Exception) cause
                : new RuntimeException(cause);

        try {
            outboxResultService.markFailed(eventId, failure);

            log.error(
                    "Failed to publish outbox event: id={}, topic={}, reason={}",
                    eventId,
                    event.getTopic(),
                    failure.getMessage(),
                    failure);

        } catch (Exception updateException) {
            log.error(
                    "Failed to update failed outbox event: id={}",
                    eventId,
                    updateException);
        }
    }

}
