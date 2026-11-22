package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import jakarta.persistence.Index;

@Entity
@Table(name = "job_descriptions", indexes = @Index(name="idx_job_description_user", columnList="user_id"))
public class JobDescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @NotBlank
    @Column(name = "role", nullable = false, length = 160)
    private String role;

    @NotBlank
    @Column(nullable=false, columnDefinition="text")
    private String description;

    @Column(name="created_at", nullable=false, updatable=false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    public JobDescription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
