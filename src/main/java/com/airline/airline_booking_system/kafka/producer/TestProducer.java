package com.airline.airline_booking_system.kafka.producer;
import com.airline.airline_booking_system.kafka.event.TestEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
@Service
public class TestProducer {
    private final KafkaTemplate<String, TestEvent> kafkaTemplate;
    public TestProducer(
            KafkaTemplate<String, TestEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void send(TestEvent event) {
        kafkaTemplate.send(
                "test-events",
                event.eventId(),
                event
        ).whenComplete((result, exception) -> {
            if (exception != null) {
                System.err.println(
                        "Failed to send event: " + exception.getMessage()
                );
            } else {
                System.out.println(
                        "Sent event at offset: "
                                + result.getRecordMetadata().offset()
                );
            }
        });
    }
}
