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
}