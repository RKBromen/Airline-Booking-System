ALTER TABLE users
    MODIFY COLUMN password_hash VARCHAR(255) NULL;

INSERT INTO roles (id, name)
VALUES
    ('00000000-0000-0000-0000-000000000002', 'ADMIN'),
    ('00000000-0000-0000-0000-000000000003', 'STAFF');

CREATE TABLE refresh_tokens (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_active
    ON refresh_tokens(user_id, revoked_at, expires_at);

CREATE TABLE user_auth_providers (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_auth_providers_identity UNIQUE (provider, provider_user_id),
    CONSTRAINT uk_user_auth_providers_user_provider UNIQUE (user_id, provider),
    CONSTRAINT fk_user_auth_providers_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_auth_providers_user
    ON user_auth_providers(user_id);
