package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import jakarta.persistence.Index;

@Entity
@Table(name = "question_sets", indexes = {@Index(name="idx_question_set_company", columnList="company_id"), @Index(name="idx_question_set_career", columnList="career_path_id"), @Index(name="idx_question_set_creator", columnList="created_by_id"), @Index(name="idx_question_set_branch_batch", columnList="branch, batch")})
public class QuestionSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "career_path_id", nullable = true)
    private CareerPath careerPath;

    @Column(name = "branch", nullable = true, length = 120)
    private String branch;

    @Column(name = "batch", nullable = true, length = 40)
    private String batch;

    @Column(name = "difficulty", nullable = true, length = 40)
    private String difficulty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    public QuestionSet() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public CareerPath getCareerPath() { return careerPath; }
    public void setCareerPath(CareerPath careerPath) { this.careerPath = careerPath; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

}
