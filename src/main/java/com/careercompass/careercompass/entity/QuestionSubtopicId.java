package com.careercompass.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuestionSubtopicId implements Serializable {

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    public QuestionSubtopicId() {}

    public QuestionSubtopicId(Long questionId, Long skillId) {
        this.questionId = questionId;
        this.skillId = skillId;
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionSubtopicId that)) return false;
        return Objects.equals(questionId, that.questionId) && Objects.equals(skillId, that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, skillId);
    }
}
