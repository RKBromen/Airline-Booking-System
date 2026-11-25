
package com.airline.airline_booking_system.outbox.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.outbox.entity.OutboxEvent;
import com.airline.airline_booking_system.outbox.enums.OutboxStatus;
import com.airline.airline_booking_system.outbox.repository.OutboxEventRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveEvent(
            String aggregateId,
            String eventType,
            String topic,
            Object event) {

        try {
            OutboxEvent outboxEvent = new OutboxEvent();

            outboxEvent.setAggregateId(aggregateId);
            outboxEvent.setEventType(eventType);
            outboxEvent.setTopic(topic);
            outboxEvent.setMessageKey(aggregateId);
            JsonNode payload = jsonMapper.valueToTree(event);
            outboxEvent.setPayload(payload);
            outboxEvent.setStatus(OutboxStatus.PENDING);
            outboxEvent.setNextRetryAt(LocalDateTime.now());
            outboxEvent.setRetryCount(0);

            outboxEventRepository.save(outboxEvent);

        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Failed to serialize outbox event", ex);
        }
    }
}
