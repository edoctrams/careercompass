package com.careercompass.careercompass.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import jakarta.persistence.Index;

@Entity
@Table(name = "roadmaps", indexes = {@Index(name="idx_roadmap_user", columnList="user_id"), @Index(name="idx_roadmap_career", columnList="career_path_id"), @Index(name="idx_roadmap_status", columnList="status")})
public class Roadmap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "career_path_id", nullable = false)
    private CareerPath careerPath;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;

    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name="created_at", nullable=false, updatable=false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "target_deadline", nullable = true)
    private LocalDate targetDeadline;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RoadmapStatus status;

    public Roadmap() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public CareerPath getCareerPath() { return careerPath; }
    public void setCareerPath(CareerPath careerPath) { this.careerPath = careerPath; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDate getTargetDeadline() { return targetDeadline; }
    public void setTargetDeadline(LocalDate targetDeadline) { this.targetDeadline = targetDeadline; }

    public RoadmapStatus getStatus() { return status; }
    public void setStatus(RoadmapStatus status) { this.status = status; }

}
