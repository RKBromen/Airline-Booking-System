package com.airline.airline_booking_system.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.nio.charset.StandardCharsets;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.notifications.email.provider", havingValue = "smtp")
public class SmtpEmailService implements EmailService {
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailService(JavaMailSender mailSender,
                            @Value("${app.notifications.email.from:}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String recipient, String subject, String textBody, String htmlBody) {
        if (fromAddress.isBlank()) {
            throw new IllegalStateException("MAIL_FROM must be set when SMTP email provider is enabled");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);
            mailSender.send(message);
        } catch (Exception failure) {
            throw new IllegalStateException("Unable to send notification email", failure);
        }
    }
}
