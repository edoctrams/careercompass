package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;
import com.careercompass.careercompass.entity.QuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSetRepository extends JpaRepository<QuestionSet, Long> {
    List<QuestionSet> findByCompanyId(Long companyId);
    List<QuestionSet> findByCareerPathId(Long careerPathId);
    List<QuestionSet> findByBranch(String branch);
    List<QuestionSet> findByBatch(String batch);
}
