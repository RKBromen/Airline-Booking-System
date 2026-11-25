
package com.airline.airline_booking_system.outbox.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.airline.airline_booking_system.outbox.entity.OutboxEvent;
import com.airline.airline_booking_system.outbox.enums.OutboxStatus;

@Repository
public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    // Find pending events ready for publishing
    List<OutboxEvent> findByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status,
            LocalDateTime now);

    // Find events stuck in PROCESSING
    List<OutboxEvent> findByStatusAndLockedUntilLessThanEqual(
            OutboxStatus status,
            LocalDateTime now);

    // Count events by status
    long countByStatus(OutboxStatus status);

    // Claim available events with row-level locking
    @Query(value = """
            SELECT *
            FROM outbox_events
            WHERE
                (
                    status = 'PENDING'
                    AND next_retry_at <= CURRENT_TIMESTAMP(6)
                )
                OR
                (
                    status = 'PROCESSING'
                    AND locked_until <= CURRENT_TIMESTAMP(6)
                )
            ORDER BY created_at ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findEventsToClaim(
            @Param("batchSize") int batchSize);

}
