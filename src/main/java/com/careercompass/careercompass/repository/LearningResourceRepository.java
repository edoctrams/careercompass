package com.careercompass.careercompass.repository;

import com.careercompass.careercompass.entity.LearningResource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LearningResourceRepository extends JpaRepository<LearningResource, Long> {
    List<LearningResource> findBySkillId(Long skillId);
    boolean existsBySkillIdAndResourceUrl(Long skillId, String resourceUrl);
}
