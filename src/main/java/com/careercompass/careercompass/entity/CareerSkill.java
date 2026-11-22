package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import jakarta.persistence.Index;

@Entity
@Table(name = "career_skills", uniqueConstraints = @UniqueConstraint(name = "uk_career_skill", columnNames = {"career_path_id", "skill_id"}), indexes = {@Index(name="idx_career_skill_path", columnList="career_path_id"), @Index(name="idx_career_skill_skill", columnList="skill_id")})
public class CareerSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "career_path_id", nullable = false)
    private CareerPath careerPath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer importance;

    @Min(0)
    @Max(5)
    @Column(name = "required_level", nullable = false)
    private Integer requiredLevel;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "prerequisite_skill_id", nullable = true)
    private Skill prerequisiteSkill;

    public CareerSkill() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CareerPath getCareerPath() { return careerPath; }
    public void setCareerPath(CareerPath careerPath) { this.careerPath = careerPath; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public Integer getImportance() { return importance; }
    public void setImportance(Integer importance) { this.importance = importance; }

    public Integer getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(Integer requiredLevel) { this.requiredLevel = requiredLevel; }

    public Skill getPrerequisiteSkill() { return prerequisiteSkill; }
    public void setPrerequisiteSkill(Skill prerequisiteSkill) { this.prerequisiteSkill = prerequisiteSkill; }

}
