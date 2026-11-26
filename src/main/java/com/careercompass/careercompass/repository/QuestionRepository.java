package com.careercompass.careercompass.repository;

import java.util.List;
import com.careercompass.careercompass.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuestionSetId(Long questionSetId);
    List<Question> findBySkillId(Long skillId);
    List<Question> findByQuestionSetIdAndDifficulty(Long questionSetId, String difficulty);

    @Query("SELECT DISTINCT qs.question FROM QuestionSubtopic qs WHERE qs.skill.id = :subtopicSkillId")
    List<Question> findBySubtopicSkillId(@Param("subtopicSkillId") Long subtopicSkillId);

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN q.companies qc WHERE (qc.company.id = :companyId OR q.questionSet.company.id = :companyId) AND q.questionSet.careerPath.id = :careerPathId")
    List<Question> findByCompanyIdAndCareerPathId(@Param("companyId") Long companyId, @Param("careerPathId") Long careerPathId);
}
