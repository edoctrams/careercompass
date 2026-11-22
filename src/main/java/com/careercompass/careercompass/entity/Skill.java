package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "category", nullable = true, length = 80)
    private String category;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "difficulty", nullable = true, length = 40)
    private String difficulty;

    public Skill() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

}
