package com.airline.airline_booking_system.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.airline.airline_booking_system.notification.entity.NotificationHistory;
import com.airline.airline_booking_system.notification.enums.NotificationStatus;
import com.airline.airline_booking_system.notification.enums.NotificationType;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, String> {
    boolean existsByEventIdAndNotificationTypeAndRecipient(
            String eventId, NotificationType notificationType, String recipient);

    List<NotificationHistory> findTop20ByStatusOrderByCreatedAtAsc(NotificationStatus status);

    @Modifying
    @Query("""
            update NotificationHistory n
               set n.status = :processing,
                   n.retryCount = n.retryCount + 1,
                   n.processingUntil = :processingUntil
             where n.id = :id and n.status = :pending
            """)
    int claimPending(@Param("id") String id,
                     @Param("pending") NotificationStatus pending,
                     @Param("processing") NotificationStatus processing,
                     @Param("processingUntil") java.time.Instant processingUntil);
}
