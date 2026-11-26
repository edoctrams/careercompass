package com.careercompass.careercompass.repository;

import com.careercompass.careercompass.entity.QuestionSubtopic;
import com.careercompass.careercompass.entity.QuestionSubtopicId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionSubtopicRepository extends JpaRepository<QuestionSubtopic, QuestionSubtopicId> {
    List<QuestionSubtopic> findByQuestionId(Long questionId);
    List<QuestionSubtopic> findBySkillId(Long skillId);
}
