package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "company_career_skills",
    uniqueConstraints = @UniqueConstraint(
        name = "company_career_skills_company_id_career_path_id_skill_id_key",
        columnNames = {"company_id", "career_path_id", "skill_id"}
    ),
    indexes = @Index(name = "idx_ccs_company_career", columnList = "company_id, career_path_id")
)
public class CompanyCareerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "career_path_id", nullable = false)
    private CareerPath careerPath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @NotNull
    @Min(0)
    @Max(5)
    @Column(name = "target_level", nullable = false)
    private Integer targetLevel;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "importance", nullable = false)
    private Integer importance;

    @NotBlank
    @Column(name = "evidence_type", nullable = false, length = 40)
    private String evidenceType = "GENERAL";

    @Column(name = "source_url", columnDefinition = "text")
    private String sourceUrl;

    @Column(name = "reviewed_at")
    private LocalDate reviewedAt;

    public CompanyCareerSkill() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public CareerPath getCareerPath() { return careerPath; }
    public void setCareerPath(CareerPath careerPath) { this.careerPath = careerPath; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public Integer getTargetLevel() { return targetLevel; }
    public void setTargetLevel(Integer targetLevel) { this.targetLevel = targetLevel; }

    public Integer getImportance() { return importance; }
    public void setImportance(Integer importance) { this.importance = importance; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public LocalDate getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDate reviewedAt) { this.reviewedAt = reviewedAt; }
}
