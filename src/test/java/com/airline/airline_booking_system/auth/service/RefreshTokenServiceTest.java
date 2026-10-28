package com.airline.airline_booking_system.auth.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.airline.airline_booking_system.auth.entity.RefreshToken;
import com.airline.airline_booking_system.auth.repository.RefreshTokenRepository;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.service.InvalidCredentialsException;

class RefreshTokenServiceTest {
    @Test
    void refreshTokensAreHashedRotatedAndSingleUse() {
        RefreshTokenRepository repository = Mockito.mock(RefreshTokenRepository.class);
        ArgumentCaptor<RefreshToken> savedToken = ArgumentCaptor.forClass(RefreshToken.class);
        doAnswer(invocation -> invocation.getArgument(0)).when(repository).save(any(RefreshToken.class));
        Users user = new Users(UUID.randomUUID().toString(), "customer@example.com", "hash", "Customer",
                Users.UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());

        RefreshTokenService service = new RefreshTokenService(repository, 30);
        String rawToken = service.issue(user);
        Mockito.verify(repository).save(savedToken.capture());
        RefreshToken stored = savedToken.getValue();
        assertNotEquals(rawToken, stored.getTokenHash());
        assertNotNull(stored.getExpiresAt());
        when(repository.findByTokenHash(anyString())).thenAnswer(invocation ->
                stored.getTokenHash().equals(invocation.getArgument(0)) ? Optional.of(stored) : Optional.empty());
        when(repository.consumeIfActive(anyString(), any())).thenReturn(1);

        assertSame(user, service.rotate(rawToken));
        assertNotNull(stored.getRevokedAt());
        when(repository.consumeIfActive(anyString(), any())).thenReturn(0);
        assertThrows(InvalidCredentialsException.class, () -> service.rotate(rawToken));
    }
}
