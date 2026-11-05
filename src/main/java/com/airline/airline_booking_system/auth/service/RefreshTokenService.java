package com.airline.airline_booking_system.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.auth.entity.RefreshToken;
import com.airline.airline_booking_system.auth.repository.RefreshTokenRepository;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.service.InvalidCredentialsException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final long expiryDays;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repository,
            @Value("${app.auth.refresh-token-expiry-days:30}") long expiryDays) {
        this.repository = repository;
        if (expiryDays < 1) throw new IllegalArgumentException("Refresh token expiry must be positive");
        this.expiryDays = expiryDays;
    }

    @Transactional
    public String issue(Users user) {
        byte[] random = new byte[48];
        secureRandom.nextBytes(random);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID().toString());
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(expiryDays));
        repository.save(token);
        return rawToken;
    }

    @Transactional
    public Users rotate(String rawToken) {
        RefreshToken current = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidCredentialsException::new);
        LocalDateTime now = LocalDateTime.now();
        if (current.getRevokedAt() != null || !current.getExpiresAt().isAfter(now)
                || current.getUser().getStatus() != Users.UserStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }
        if (repository.consumeIfActive(current.getTokenHash(), now) != 1) {
            throw new InvalidCredentialsException();
        }
        current.setRevokedAt(now);
        return current.getUser();
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        repository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) token.setRevokedAt(LocalDateTime.now());
        });
    }

    @Transactional
    public void revokeAll(String userId) {
        repository.revokeAllForUser(userId);
    }

    public long getExpirySeconds() { return expiryDays * 24 * 60 * 60; }

    private String hash(String rawToken) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
