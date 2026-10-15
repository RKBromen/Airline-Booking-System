package com.airline.airline_booking_system.kafka.controller;

import java.util.UUID;

import com.airline.airline_booking_system.kafka.event.TestEvent;
import com.airline.airline_booking_system.kafka.producer.TestProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/kafka/test")
@Tag(name = "Kafka Test", description = "Send test messages to Kafka")
public class KafkaTestController {
    private final TestProducer testProducer;

    public KafkaTestController(TestProducer testProducer) {
        this.testProducer = testProducer;
    }

    @PostMapping
    @Operation(summary = "Send a test message",
            description = "Submits an event to the test-events topic asynchronously. "
                    + "A 202 response does not confirm broker acknowledgement.")
    @ApiResponse(responseCode = "202", description = "Event submitted for asynchronous sending")
    @ApiResponse(responseCode = "400", description = "Missing, blank or invalid message", useReturnTypeSchema = false)
    public ResponseEntity<TestEvent> send(@RequestBody MessageRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Message must not be blank");
        }

        TestEvent event = new TestEvent(UUID.randomUUID().toString(), request.message());
        testProducer.send(event);
        return ResponseEntity.accepted().body(event);
    }

    public record MessageRequest(
            @Schema(description = "Message to send to Kafka", example = "Hello Kafka!", requiredMode = Schema.RequiredMode.REQUIRED)
            String message) {
    }
}
