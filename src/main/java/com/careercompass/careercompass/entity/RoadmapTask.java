package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import jakarta.persistence.Index;

@Entity
@Table(name = "roadmap_tasks", indexes = {@Index(name="idx_task_roadmap", columnList="roadmap_id"), @Index(name="idx_task_skill", columnList="skill_id"), @Index(name="idx_task_status", columnList="status")})
public class RoadmapTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "skill_id", nullable = true)
    private Skill skill;

    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(columnDefinition="text")
    private String description;

    @Min(1)
    @Column(name="week_number")
    private Integer weekNumber;

    @Min(0)
    @Column(name="estimated_hours")
    private Integer estimatedHours;

    @Size(max=2048)
    @Column(name="resource_url", length=2048)
    private String resourceUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=40)
    private RoadmapTaskStatus status;

    public RoadmapTask() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Roadmap getRoadmap() { return roadmap; }
    public void setRoadmap(Roadmap roadmap) { this.roadmap = roadmap; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }

    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }

    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }

    public RoadmapTaskStatus getStatus() { return status; }
    public void setStatus(RoadmapTaskStatus status) { this.status = status; }

}
