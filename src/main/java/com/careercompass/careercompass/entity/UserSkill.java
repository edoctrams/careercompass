package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import jakarta.persistence.Index;

@Entity
@Table(name = "user_skills", uniqueConstraints = @UniqueConstraint(name = "uk_user_skill", columnNames = {"user_id", "skill_id"}), indexes = {@Index(name="idx_user_skill_user", columnList="user_id")})
public class UserSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @NotNull
    @Min(0)
    @Max(5)
    @Column(nullable = false)
    private Integer proficiency;

    @Column(name = "last_updated", nullable = false)
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime lastUpdated;

    public UserSkill() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public Integer getProficiency() { return proficiency; }
    public void setProficiency(Integer proficiency) { this.proficiency = proficiency; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

}
