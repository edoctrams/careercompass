package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.math.BigDecimal;
import jakarta.persistence.Index;

@Entity
@Table(name = "questions", indexes = {@Index(name="idx_question_set", columnList="question_set_id"), @Index(name="idx_question_skill", columnList="skill_id")})
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_set_id", nullable = false)
    private QuestionSet questionSet;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "skill_id", nullable = true)
    private Skill skill;

    @NotBlank
    @Column(name="question_text", nullable=false, columnDefinition="text")
    private String questionText;

    @Column(columnDefinition="text")
    private String answer;

    @Column(name = "difficulty", nullable = true, length = 40)
    private String difficulty;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="question_type", nullable=false, length=30)
    private QuestionType questionType;

    public Question() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuestionSet getQuestionSet() { return questionSet; }
    public void setQuestionSet(QuestionSet questionSet) { this.questionSet = questionSet; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

}
