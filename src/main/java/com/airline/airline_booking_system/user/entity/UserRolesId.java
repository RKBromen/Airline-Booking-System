package com.airline.airline_booking_system.user.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserRolesId implements Serializable {
    @Column(name = "user_id", nullable = false)
    private String user_id;
    @Column(name = "role_id", nullable = false)
    private String role_id;

    public UserRolesId() {
    }

    public UserRolesId(String user_id, String role_id) {
        this.user_id = user_id;
        this.role_id = role_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getRole_id() {
        return role_id;
    }

    public void setRole_id(String role_id) {
        this.role_id = role_id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserRolesId that)) {
            return false;
        }
        return Objects.equals(user_id, that.user_id) && Objects.equals(role_id, that.role_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id, role_id);
    }
}
