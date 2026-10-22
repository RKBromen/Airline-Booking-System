package com.airline.airline_booking_system.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import com.airline.airline_booking_system.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now where t.tokenHash = :tokenHash and t.revokedAt is null and t.expiresAt > :now")
    int consumeIfActive(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = CURRENT_TIMESTAMP where t.user.id = :userId and t.revokedAt is null")
    int revokeAllForUser(@Param("userId") String userId);

    List<RefreshToken> findAllByUser_IdAndRevokedAtIsNull(String userId);
}
