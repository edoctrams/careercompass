package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "career_interest_assessments",
    indexes = {
        @Index(
            name = "idx_career_assessment_user",
            columnList = "user_id"
        )
    }
)
public class CareerInterestAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "answers_json", columnDefinition = "text", nullable = false)
    private String answersJson;

    @Column(name = "scores_json", columnDefinition = "text", nullable = false)
    private String scoresJson;

    @Column(name = "recommended_career_name", nullable = false, length = 160)
    private String recommendedCareerName;

    @Column(name = "selected_career_name", nullable = false, length = 160)
    private String selectedCareerName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CareerInterestAssessment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAnswersJson() {
        return answersJson;
    }

    public void setAnswersJson(String answersJson) {
        this.answersJson = answersJson;
    }

    public String getScoresJson() {
        return scoresJson;
    }

    public void setScoresJson(String scoresJson) {
        this.scoresJson = scoresJson;
    }

    public String getRecommendedCareerName() {
        return recommendedCareerName;
    }

    public void setRecommendedCareerName(String recommendedCareerName) {
        this.recommendedCareerName = recommendedCareerName;
    }

    public String getSelectedCareerName() {
        return selectedCareerName;
    }

    public void setSelectedCareerName(String selectedCareerName) {
        this.selectedCareerName = selectedCareerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
