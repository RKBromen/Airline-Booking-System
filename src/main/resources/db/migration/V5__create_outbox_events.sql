CREATE TABLE outbox_events (
    id CHAR(36) NOT NULL,

    aggregate_id VARCHAR(64) NOT NULL,

    event_type VARCHAR(100) NOT NULL,

    event_version INT NOT NULL DEFAULT 1,

    topic VARCHAR(255) NOT NULL,

    message_key VARCHAR(255) NOT NULL,

    payload JSON NOT NULL,

    status ENUM('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED') NOT NULL DEFAULT 'PENDING',

    retry_count INT NOT NULL DEFAULT 0,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    published_at DATETIME(6) NULL,

    locked_until DATETIME(6) NULL,

    next_retry_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    last_error TEXT NULL,

    PRIMARY KEY (id),

    CONSTRAINT chk_outbox_retry_count
        CHECK (retry_count >= 0),

    INDEX idx_outbox_pending (
        status,
        next_retry_at,
        created_at
    ),

    INDEX idx_outbox_locked (
        status,
        locked_until
    ),

    INDEX idx_outbox_aggregate (
        aggregate_id
    )

);
