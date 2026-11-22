package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;
import com.careercompass.careercompass.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuestionSetId(Long questionSetId);
    List<Question> findBySkillId(Long skillId);
    List<Question> findByQuestionSetIdAndDifficulty(Long questionSetId, String difficulty);
}
