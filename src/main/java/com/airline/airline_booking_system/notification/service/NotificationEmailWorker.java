package com.airline.airline_booking_system.notification.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.airline.airline_booking_system.notification.entity.NotificationHistory;

@Component
public class NotificationEmailWorker {
    private static final Logger log = LoggerFactory.getLogger(NotificationEmailWorker.class);
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final NotificationEmailTemplate emailTemplate;

    public NotificationEmailWorker(NotificationService notificationService, EmailService emailService,
                                   NotificationEmailTemplate emailTemplate) {
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.emailTemplate = emailTemplate;
    }

    @Scheduled(fixedDelayString = "${app.notifications.worker.poll-interval-ms:5000}")
    public void processPending() {
        List<NotificationHistory> pending = notificationService.pendingBatch();
        for (NotificationHistory candidate : pending) {
            notificationService.claim(candidate.getId()).ifPresent(this::send);
        }
    }

    private void send(NotificationHistory notification) {
        try {
            RenderedEmail email = emailTemplate.render(notification);
            emailService.send(notification.getRecipient(), email.subject(), email.textBody(), email.htmlBody());
            notificationService.markSent(notification.getId());
        } catch (Exception failure) {
            log.warn("Notification email failed: notificationId={}, type={}",
                    notification.getId(), notification.getNotificationType(), failure);
            notificationService.markFailed(notification.getId(), failure);
        }
    }

}
