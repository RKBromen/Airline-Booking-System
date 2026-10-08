package com.airline.airline_booking_system.role.entity;

import com.airline.airline_booking_system.user.entity.Users;

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
    private Users user;

    @ManyToOne
    @MapsId("role_id")
    @JoinColumn(name = "role_id")
    private Roles role;

    public UserRoles() {
    }

    public UserRoles(UserRolesId id, Users user, Roles role) {
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

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }
}
