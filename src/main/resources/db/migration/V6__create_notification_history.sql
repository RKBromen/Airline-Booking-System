CREATE TABLE notification_history (
    id CHAR(36) NOT NULL PRIMARY KEY,
    event_id CHAR(36) NOT NULL,
    booking_id CHAR(36) NULL,
    recipient VARCHAR(255) NOT NULL,
    recipient_name VARCHAR(150) NOT NULL,
    booking_code VARCHAR(32) NULL,
    flight_number VARCHAR(20) NULL,
    departure_time TIMESTAMP(6) NULL,
    notification_type VARCHAR(32) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    sent_at TIMESTAMP(6) NULL,
    last_error VARCHAR(2000) NULL,
    processing_until TIMESTAMP(6) NULL,

    CONSTRAINT uk_notification_event_type_recipient
        UNIQUE (event_id, notification_type, recipient),
    CONSTRAINT chk_notification_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED')),
    CONSTRAINT chk_notification_type
        CHECK (notification_type IN ('BOOKING_CONFIRMED', 'BOOKING_CANCELLED', 'ACCOUNT_REGISTERED'))
);

CREATE INDEX idx_notification_status_created
    ON notification_history(status, created_at);
