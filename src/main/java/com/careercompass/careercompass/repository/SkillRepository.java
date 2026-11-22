package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;
import com.careercompass.careercompass.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
    List<Skill> findByCategoryIgnoreCase(String category);
    List<Skill> findByCategory(String category);
        Optional<Skill> findByNameIgnoreCase(String name);
}
