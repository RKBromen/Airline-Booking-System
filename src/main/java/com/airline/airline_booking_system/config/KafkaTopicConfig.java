package com.airline.airline_booking_system.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic testTopic() {
        return new NewTopic(
                "test-events",
                3,
                (short) 1
        );
    }

    @Bean
    public NewTopic bookingConfirmedTopic() {
        return new NewTopic("booking.confirmed", 3, (short) 1);
    }

    @Bean
    public NewTopic bookingCancelledTopic() {
        return new NewTopic("booking.cancelled", 3, (short) 1);
    }

    @Bean
    public NewTopic userRegisteredTopic() {
        return new NewTopic("user.registered", 3, (short) 1);
    }
}
