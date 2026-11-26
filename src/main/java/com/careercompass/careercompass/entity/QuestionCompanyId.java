package com.careercompass.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuestionCompanyId implements Serializable {

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    public QuestionCompanyId() {}

    public QuestionCompanyId(Long questionId, Long companyId) {
        this.questionId = questionId;
        this.companyId = companyId;
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionCompanyId that)) return false;
        return Objects.equals(questionId, that.questionId) && Objects.equals(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, companyId);
    }
}
