package com.airline.airline_booking_system.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Roles {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    public Roles(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
