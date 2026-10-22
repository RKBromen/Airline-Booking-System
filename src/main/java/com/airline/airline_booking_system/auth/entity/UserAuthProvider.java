package com.airline.airline_booking_system.auth.entity;

import java.time.LocalDateTime;

import com.airline.airline_booking_system.auth.enums.AuthProvider;
import com.airline.airline_booking_system.user.entity.Users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_auth_providers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_auth_providers_identity", columnNames = { "provider", "provider_user_id" }),
        @UniqueConstraint(name = "uk_user_auth_providers_user_provider", columnNames = { "user_id", "provider" })
})
@Getter
@Setter
@NoArgsConstructor
public class UserAuthProvider {
    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
