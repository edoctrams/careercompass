package com.careercompass.careercompass.repository;

import com.careercompass.careercompass.entity.SkillProgressHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillProgressHistoryRepository extends JpaRepository<SkillProgressHistory, Long> {
    List<SkillProgressHistory> findByUserIdAndSkillIdOrderByRecordedAtDesc(Long userId, Long skillId);
    List<SkillProgressHistory> findByUserIdOrderByRecordedAtDesc(Long userId);
}
