package com.airline.airline_booking_system.kafka.producer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.airline.airline_booking_system.outbox.entity.OutboxEvent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class OutboxKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxKafkaPublisher(
            @Qualifier("outboxKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, String>> publish(
            OutboxEvent event) {
        String payload = event.getPayload().toString();

        return kafkaTemplate.send(
                event.getTopic(),
                event.getMessageKey(),
                payload);
    }
}
