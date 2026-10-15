package com.airline.airline_booking_system.role.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Embeddable
public class UserRolesId implements Serializable {
    @Column(nullable = false)
    private String user_id;
    @Column(nullable = false)
    private String role_id;

    public UserRolesId(String user_id, String role_id) {
        this.user_id = user_id;
        this.role_id = role_id;
    }
}
