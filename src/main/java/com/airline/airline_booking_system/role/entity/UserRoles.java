package com.airline.airline_booking_system.role.entity;

import org.apache.catalina.User;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_roles")
public class UserRoles {
    @EmbeddedId
    private UserRolesId id;

    @ManyToOne
    @MapsId("user_id")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("role_id")
    @JoinColumn(name = "role_id")
    private User role;

    public UserRoles() {
    }

    public UserRoles(UserRolesId id, User user, User role) {
        this.id = id;
        this.user = user;
        this.role = role;
    }

    public UserRolesId getId() {
        return id;
    }

    public void setId(UserRolesId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getRole() {
        return role;
    }

    public void setRole(User role) {
        this.role = role;
    }
}
