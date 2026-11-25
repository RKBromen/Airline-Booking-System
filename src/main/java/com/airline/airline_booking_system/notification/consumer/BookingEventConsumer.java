package com.airline.airline_booking_system.notification.consumer;

import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.airline.airline_booking_system.notification.enums.NotificationType;
import com.airline.airline_booking_system.notification.event.BookingCancelledEvent;
import com.airline.airline_booking_system.notification.event.BookingConfirmedEvent;
import com.airline.airline_booking_system.notification.event.BookingNotificationEvent;
import com.airline.airline_booking_system.notification.event.UserRegisteredEvent;
import com.airline.airline_booking_system.notification.service.NotificationService;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Component
public class BookingEventConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Validator validator;
    private final NotificationService notificationService;

    public BookingEventConsumer(Validator validator, NotificationService notificationService) {
        this.validator = validator;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.notifications.kafka.topics.booking-confirmed:booking.confirmed}",
            containerFactory = "notificationKafkaListenerContainerFactory")
    public void onBookingConfirmed(String json) {
        BookingConfirmedEvent event = deserialize(json, BookingConfirmedEvent.class, "BOOKING_CONFIRMED");
        validate(event);
        notificationService.accept(event, NotificationType.BOOKING_CONFIRMED);
    }

    @KafkaListener(topics = "${app.notifications.kafka.topics.booking-cancelled:booking.cancelled}",
            containerFactory = "notificationKafkaListenerContainerFactory")
    public void onBookingCancelled(String json) {
        BookingCancelledEvent event = deserialize(json, BookingCancelledEvent.class, "BOOKING_CANCELLED");
        validate(event);
        notificationService.accept(event, NotificationType.BOOKING_CANCELLED);
    }

    @KafkaListener(topics = "${app.notifications.kafka.topics.user-registered:user.registered}",
            containerFactory = "notificationKafkaListenerContainerFactory")
    public void onUserRegistered(String json) {
        UserRegisteredEvent event = deserializeUserRegistered(json);
        Set<ConstraintViolation<UserRegisteredEvent>> violations = validator.validate(event);
        if (event.eventVersion() != 1) {
            throw new IllegalArgumentException("Unsupported user registration event version: " + event.eventVersion());
        }
        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid user registration event: " + details);
        }
        notificationService.accept(event);
    }

    private UserRegisteredEvent deserializeUserRegistered(String json) {
        try {
            return objectMapper.readValue(json, UserRegisteredEvent.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid user registration event JSON", exception);
        }
    }

    private <T extends BookingNotificationEvent> T deserialize(
            String json, Class<T> eventClass, String expectedEventType) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("payload") && root.get("payload").isObject()) {
                String eventType = root.path("eventType").asText();
                if (!expectedEventType.equals(eventType)) {
                    throw new IllegalArgumentException("Event type does not match Kafka topic");
                }
                ObjectNode payload = ((ObjectNode) root.get("payload")).deepCopy();
                payload.put("eventId", root.path("eventId").asText());
                payload.put("eventVersion", root.path("eventVersion").asInt());
                payload.put("bookingId", root.path("aggregateId").asText());
                return objectMapper.treeToValue(payload, eventClass);
            }
            return objectMapper.treeToValue(root, eventClass);
        } catch (Exception exception) {
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            throw new IllegalArgumentException("Invalid booking event JSON", exception);
        }
    }

    private void validate(BookingNotificationEvent event) {
        Set<ConstraintViolation<BookingNotificationEvent>> violations = validator.validate(event);
        if (event.eventVersion() != 1) {
            throw new IllegalArgumentException("Unsupported booking event version: " + event.eventVersion());
        }
        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid booking event: " + details);
        }
    }
}
