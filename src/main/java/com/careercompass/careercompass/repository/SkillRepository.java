package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;
import com.careercompass.careercompass.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
    Optional<Skill> findByNameIgnoreCase(String name);
    List<Skill> findByCategoryIgnoreCase(String category);
    List<Skill> findByCategory(String category);

    List<Skill> findByNodeType(String nodeType);
    List<Skill> findByParentSkillId(Long parentSkillId);

    @Query("SELECT cs.skill FROM CareerSkill cs WHERE cs.careerPath.id = :careerPathId AND cs.skill.nodeType = 'TOPIC' ORDER BY cs.importance DESC, cs.skill.name ASC")
    List<Skill> findTopicsByCareerPathId(@Param("careerPathId") Long careerPathId);

    @Query("SELECT cs.skill FROM CareerSkill cs WHERE cs.careerPath.id = :careerPathId AND cs.skill.nodeType = 'SUBTOPIC' ORDER BY cs.skill.parentSkill.id, cs.importance DESC, cs.skill.name ASC")
    List<Skill> findSubtopicsByCareerPathId(@Param("careerPathId") Long careerPathId);

    @Query("SELECT cs.skill FROM CareerSkill cs WHERE cs.careerPath.id = :careerPathId AND cs.skill.parentSkill.id = :parentSkillId ORDER BY cs.importance DESC, cs.skill.name ASC")
    List<Skill> findSubtopicsByCareerPathIdAndParentSkillId(@Param("careerPathId") Long careerPathId, @Param("parentSkillId") Long parentSkillId);
}
