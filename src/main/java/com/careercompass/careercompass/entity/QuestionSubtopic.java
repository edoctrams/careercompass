package com.careercompass.careercompass.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "question_subtopics", indexes = @Index(name = "idx_qsub_skill", columnList = "skill_id"))
public class QuestionSubtopic {

    @EmbeddedId
    private QuestionSubtopicId id = new QuestionSubtopicId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("questionId")
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    public QuestionSubtopic() {}

    public QuestionSubtopic(Question question, Skill skill) {
        this.question = question;
        this.skill = skill;
        this.id = new QuestionSubtopicId(question.getId(), skill.getId());
    }

    public QuestionSubtopicId getId() { return id; }
    public void setId(QuestionSubtopicId id) { this.id = id; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }
}
