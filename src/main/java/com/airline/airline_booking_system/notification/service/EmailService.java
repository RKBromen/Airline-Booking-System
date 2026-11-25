package com.airline.airline_booking_system.notification.service;

public interface EmailService {
    void send(String recipient, String subject, String textBody, String htmlBody);
}
