package com.careercompass.careercompass.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skills", indexes = @Index(name = "idx_skills_parent_node", columnList = "parent_skill_id, node_type"))
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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "parent_skill_id", nullable = true)
    private Skill parentSkill;

    @JsonIgnore
    @OneToMany(mappedBy = "parentSkill", fetch = FetchType.LAZY)
    private List<Skill> childSkills = new ArrayList<>();

    @NotBlank
    @Column(name = "node_type", nullable = false, length = 40)
    private String nodeType = "TOPIC";

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

    public Skill getParentSkill() { return parentSkill; }
    public void setParentSkill(Skill parentSkill) { this.parentSkill = parentSkill; }

    public List<Skill> getChildSkills() { return childSkills; }
    public void setChildSkills(List<Skill> childSkills) { this.childSkills = childSkills; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
}
