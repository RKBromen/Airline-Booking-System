package com.airline.airline_booking_system.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.notifications.email.provider", havingValue = "mock", matchIfMissing = true)
public class MockEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(MockEmailService.class);

    @Override
    public void send(String recipient, String subject, String textBody, String htmlBody) {
        log.info("Mock email provider accepted notification: recipient={}, subject={}", recipient, subject);
    }
}
