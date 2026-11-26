package com.careercompass.careercompass.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "skill_progress_history",
    indexes = @Index(name = "idx_sph_user_skill", columnList = "user_id, skill_id, recorded_at")
)
public class SkillProgressHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Min(0)
    @Max(5)
    @Column(name = "previous_level")
    private Integer previousLevel;

    @NotNull
    @Min(0)
    @Max(5)
    @Column(name = "new_level", nullable = false)
    private Integer newLevel;

    @NotBlank
    @Column(name = "source", nullable = false, length = 40)
    private String source = "ASSESSMENT";

    @Column(name = "recorded_at", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime recordedAt;

    public SkillProgressHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public Integer getPreviousLevel() { return previousLevel; }
    public void setPreviousLevel(Integer previousLevel) { this.previousLevel = previousLevel; }

    public Integer getNewLevel() { return newLevel; }
    public void setNewLevel(Integer newLevel) { this.newLevel = newLevel; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
