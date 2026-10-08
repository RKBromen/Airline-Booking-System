package com.airline.airline_booking_system.user.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.airline.airline_booking_system.user.entity.Users;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtTokenService {
    private static final String ISSUER = "airline-booking-system";

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION_SECONDS must be positive");
        }
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(Users user, List<String> roles) {
        Instant issuedAt = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(issuedAt.plusSeconds(expirationSeconds)))
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT)
                .build();
        SignedJWT signedJwt = new SignedJWT(header, claims);
        try {
            signedJwt.sign(new MACSigner(secret));
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Could not sign access token", exception);
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
