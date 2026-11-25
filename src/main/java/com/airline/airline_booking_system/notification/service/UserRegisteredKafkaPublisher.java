package com.airline.airline_booking_system.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.airline.airline_booking_system.notification.event.UserRegisteredDomainEvent;
import com.airline.airline_booking_system.notification.event.UserRegisteredEvent;

@Component
public class UserRegisteredKafkaPublisher {
    private static final Logger log = LoggerFactory.getLogger(UserRegisteredKafkaPublisher.class);

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;
    private final String topic;

    public UserRegisteredKafkaPublisher(
            KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate,
            @Value("${app.notifications.kafka.topics.user-registered:user.registered}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(UserRegisteredDomainEvent registration) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                registration.eventId(), 1, registration.userId(), registration.email(),
                registration.fullName(), registration.registeredAt());
        kafkaTemplate.send(topic, registration.userId(), event).whenComplete((result, failure) -> {
            if (failure != null) {
                log.error("Could not publish user registration event: eventId={}", registration.eventId(), failure);
            } else {
                log.info("Published user registration event: eventId={}, topic={}, partition={}, offset={}",
                        registration.eventId(), topic, result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
