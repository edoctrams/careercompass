package com.careercompass.careercompass.repository;

import com.careercompass.careercompass.entity.QuestionCompany;
import com.careercompass.careercompass.entity.QuestionCompanyId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionCompanyRepository extends JpaRepository<QuestionCompany, QuestionCompanyId> {
    List<QuestionCompany> findByQuestionId(Long questionId);
    List<QuestionCompany> findByCompanyId(Long companyId);
}
