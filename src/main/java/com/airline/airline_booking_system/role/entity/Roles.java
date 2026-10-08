package com.airline.airline_booking_system.role.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Roles {
    @Id
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(nullable = false)
    private String name;

    public Roles() {
    }

    public Roles(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
