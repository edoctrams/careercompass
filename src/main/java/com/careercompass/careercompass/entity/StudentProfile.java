package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import jakarta.persistence.Index;

@Entity
@Table(name = "student_profiles", indexes = @Index(name = "idx_student_profile_target_role", columnList = "target_role_id"))
public class StudentProfile {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_student_profile_user"))
    private User user;

    @Column(name = "branch", nullable = true, length = 120)
    private String branch;

    @Min(1900)
    @Column(name = "graduation_year", nullable = true)
    private Integer graduationYear;

    @Min(1)
    @Column(name = "current_year", nullable = true)
    private Integer currentYear;

    @Min(0)
    @Max(24)
    @Column(name = "daily_available_hours", nullable = true)
    private Integer dailyAvailableHours;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "target_role_id", nullable = true)
    private CareerPath targetRole;

    @Column(name = "career_goal", columnDefinition = "text")
    private String careerGoal;

    public StudentProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public Integer getCurrentYear() { return currentYear; }
    public void setCurrentYear(Integer currentYear) { this.currentYear = currentYear; }

    public Integer getDailyAvailableHours() { return dailyAvailableHours; }
    public void setDailyAvailableHours(Integer dailyAvailableHours) { this.dailyAvailableHours = dailyAvailableHours; }

    public CareerPath getTargetRole() { return targetRole; }
    public void setTargetRole(CareerPath targetRole) { this.targetRole = targetRole; }

    public String getCareerGoal() { return careerGoal; }
    public void setCareerGoal(String careerGoal) { this.careerGoal = careerGoal; }

}
