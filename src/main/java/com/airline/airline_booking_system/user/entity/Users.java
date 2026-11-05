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
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)")
    private String id;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash")
    private String password;
    @Column(name = "full_name", nullable = false, length = 150)
    private String full_name;

    public enum UserStatus {
        ACTIVE, BLOCKED, DELETED
    };

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;
    @Column(name = "updated_at", nullable = false)
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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFull_name() { return full_name; }
    public void setFull_name(String fullName) { this.full_name = fullName; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime createdAt) { this.created_at = createdAt; }
    public LocalDateTime getUpdated_at() { return updated_at; }
    public void setUpdated_at(LocalDateTime updatedAt) { this.updated_at = updatedAt; }
}
