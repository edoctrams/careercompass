package com.careercompass.careercompass.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class StudentProfileDTO {

    private Long id;
    private String name;
    private String email;
    private String branch;
    private Integer graduationYear;
    private Integer currentYear;
    private Integer dailyAvailableHours;
    private String careerGoal;
    private String targetRoleName;
    private LocalDate createdAt;
    private boolean profileComplete;

    public StudentProfileDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public Integer getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(Integer currentYear) {
        this.currentYear = currentYear;
    }

    public Integer getDailyAvailableHours() {
        return dailyAvailableHours;
    }

    public void setDailyAvailableHours(Integer dailyAvailableHours) {
        this.dailyAvailableHours = dailyAvailableHours;
    }

    public String getCareerGoal() {
        return careerGoal;
    }

    public void setCareerGoal(String careerGoal) {
        this.careerGoal = careerGoal;
    }

    public String getTargetRoleName() {
        return targetRoleName;
    }

    public void setTargetRoleName(String targetRoleName) {
        this.targetRoleName = targetRoleName;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }
}