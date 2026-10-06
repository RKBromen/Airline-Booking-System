package com.airline.airline_booking_system.role.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserRolesId implements Serializable {
    @Column(nullable = false)
    private String user_id;
    @Column(nullable = false)
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
}
