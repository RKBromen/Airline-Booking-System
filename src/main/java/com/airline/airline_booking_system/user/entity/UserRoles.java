package com.airline.airline_booking_system.user.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

    public UserRoles(UserRolesId id, Users user, Roles role) {
        this.id = id;
        this.user = user;
        this.role = role;
    }
}
