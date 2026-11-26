package com.careercompass.careercompass.repository;

import com.careercompass.careercompass.entity.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {
    List<QuestionAttempt> findByUserIdOrderByAttemptedAtDesc(Long userId);
    List<QuestionAttempt> findByUserIdAndQuestionIdOrderByAttemptedAtDesc(Long userId, Long questionId);
}
