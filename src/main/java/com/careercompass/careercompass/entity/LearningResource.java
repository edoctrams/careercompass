package com.careercompass.careercompass.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "learning_resources",
    uniqueConstraints = @UniqueConstraint(name = "uk_learning_resource_skill_url", columnNames = {"skill_id", "resource_url"}),
    indexes = @Index(name = "idx_lr_skill", columnList = "skill_id")
)
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @NotBlank
    @Size(max = 2048)
    @Column(name = "resource_url", nullable = false, length = 2048)
    private String resourceUrl;

    @NotBlank
    @Column(name = "resource_type", nullable = false, length = 40)
    private String resourceType;

    @Column(name = "difficulty", length = 40)
    private String difficulty;

    @Column(name = "language", length = 40)
    private String language;

    @Column(name = "provider", length = 120)
    private String provider;

    @Column(name = "created_at", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    public LearningResource() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
