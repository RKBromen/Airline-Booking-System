package com.airline.airline_booking_system.kafka.consumer;
import com.airline.airline_booking_system.kafka.event.TestEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
public class TestConsumer {
    @KafkaListener(topics = "test-events")
    public void consume(TestEvent event) {
        System.out.println(
                "Received event: " + event
        );
    }
}
