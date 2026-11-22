package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "career_paths")
public class CareerPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, unique = true, length = 160)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "difficulty", nullable = true, length = 40)
    private String difficulty;

    @Min(0)
    @Column(name = "average_preparation_months", nullable = true)
    private Integer averagePreparationMonths;

    public CareerPath() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getAveragePreparationMonths() { return averagePreparationMonths; }
    public void setAveragePreparationMonths(Integer averagePreparationMonths) { this.averagePreparationMonths = averagePreparationMonths; }

}
