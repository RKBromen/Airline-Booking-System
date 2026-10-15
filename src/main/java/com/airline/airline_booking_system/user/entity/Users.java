package com.airline.airline_booking_system.user.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.booking.entity.Bookings;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "users")
public class Users {
    @Id
    private String id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String full_name;

    public enum UserStatus {
        ACTIVE, BLOCKED, DELETED
    };

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    @Column(nullable = false)
    private LocalDateTime created_at;
    @Column(nullable = false)
    private LocalDateTime updated_at;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private List<Bookings> bookings;

    public Users(String id, String email, String password, String full_name, UserStatus status,
            LocalDateTime created_at,
            LocalDateTime updated_at) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.full_name = full_name;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
}
