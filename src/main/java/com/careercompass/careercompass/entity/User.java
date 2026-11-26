package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @NotBlank
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
