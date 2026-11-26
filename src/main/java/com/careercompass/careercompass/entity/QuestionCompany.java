package com.careercompass.careercompass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "question_companies", indexes = @Index(name = "idx_qcomp_company", columnList = "company_id"))
public class QuestionCompany {

    @EmbeddedId
    private QuestionCompanyId id = new QuestionCompanyId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("questionId")
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank
    @Column(name = "evidence_type", nullable = false, length = 40)
    private String evidenceType = "GENERAL";

    @Column(name = "source_url", columnDefinition = "text")
    private String sourceUrl;

    public QuestionCompany() {}

    public QuestionCompany(Question question, Company company, String evidenceType, String sourceUrl) {
        this.question = question;
        this.company = company;
        this.evidenceType = evidenceType != null ? evidenceType : "GENERAL";
        this.sourceUrl = sourceUrl;
        this.id = new QuestionCompanyId(question.getId(), company.getId());
    }

    public QuestionCompanyId getId() { return id; }
    public void setId(QuestionCompanyId id) { this.id = id; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}
