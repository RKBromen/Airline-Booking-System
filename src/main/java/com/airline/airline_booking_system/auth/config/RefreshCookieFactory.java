package com.airline.airline_booking_system.auth.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.airline.airline_booking_system.auth.service.RefreshTokenService;

@Component
public class RefreshCookieFactory {
    public static final String COOKIE_NAME = "refresh_token";
    private final RefreshTokenService tokenService;
    private final boolean secure;

    public RefreshCookieFactory(RefreshTokenService tokenService,
            @Value("${app.auth.cookie-secure:false}") boolean secure) {
        this.tokenService = tokenService;
        this.secure = secure;
    }

    public ResponseCookie issue(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true).secure(secure).sameSite("Lax")
                .path("/api/v1/auth").maxAge(Duration.ofSeconds(tokenService.getExpirySeconds())).build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true).secure(secure).sameSite("Lax")
                .path("/api/v1/auth").maxAge(Duration.ZERO).build();
    }
}
