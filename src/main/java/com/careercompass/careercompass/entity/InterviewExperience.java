package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import jakarta.persistence.Index;

@Entity
@Table(name = "interview_experiences", indexes = {@Index(name="idx_experience_company", columnList="company_id"), @Index(name="idx_experience_user", columnList="user_id")})
public class InterviewExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "role", nullable = true, length = 160)
    private String role;

    @Column(name = "batch", nullable = true, length = 40)
    private String batch;

    @NotBlank
    @Column(name="experience_text", nullable=false, columnDefinition="text")
    private String experienceText;

    @Column(name = "interview_date", nullable = true)
    private LocalDate interviewDate;

    @Column(name = "difficulty", nullable = true, length = 40)
    private String difficulty;

    public InterviewExperience() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public String getExperienceText() { return experienceText; }
    public void setExperienceText(String experienceText) { this.experienceText = experienceText; }

    public LocalDate getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDate interviewDate) { this.interviewDate = interviewDate; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

}
