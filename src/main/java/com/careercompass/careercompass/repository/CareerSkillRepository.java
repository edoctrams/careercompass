package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;
import com.careercompass.careercompass.entity.CareerSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerSkillRepository extends JpaRepository<CareerSkill, Long> {
    List<CareerSkill> findByCareerPathId(Long careerPathId);
    Optional<CareerSkill> findByCareerPathIdAndSkillId(Long careerPathId, Long skillId);
    boolean existsByCareerPathIdAndSkillId(Long careerPathId, Long skillId);
}
