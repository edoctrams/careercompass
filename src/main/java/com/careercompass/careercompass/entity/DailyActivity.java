package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import jakarta.persistence.Index;

@Entity
@Table(name = "daily_activity", uniqueConstraints = @UniqueConstraint(name="uk_daily_activity", columnNames={"user_id", "activity_date", "activity_type"}), indexes = @Index(name="idx_daily_activity_user_date", columnList="user_id, activity_date"))
public class DailyActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name="activity_date", nullable=false)
    private LocalDate activityDate;

    @NotBlank
    @Column(name="activity_type", nullable=false, length=80)
    private String activityType;

    public DailyActivity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

}
