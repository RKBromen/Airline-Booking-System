package com.airline.airline_booking_system.kafka.controller;

import java.util.UUID;

import com.airline.airline_booking_system.kafka.event.TestEvent;
import com.airline.airline_booking_system.kafka.producer.TestProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/kafka/test")
public class KafkaTestController {
    private final TestProducer testProducer;

    public KafkaTestController(TestProducer testProducer) {
        this.testProducer = testProducer;
    }

    @PostMapping
    public ResponseEntity<TestEvent> send(@RequestBody MessageRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Message must not be blank");
        }

        TestEvent event = new TestEvent(UUID.randomUUID().toString(), request.message());
        testProducer.send(event);
        return ResponseEntity.accepted().body(event);
    }

    public record MessageRequest(String message) {
    }
}
