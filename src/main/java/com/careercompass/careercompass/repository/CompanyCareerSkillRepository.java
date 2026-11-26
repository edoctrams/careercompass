package com.careercompass.careercompass.repository;

import com.careercompass.careercompass.entity.CompanyCareerSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompanyCareerSkillRepository extends JpaRepository<CompanyCareerSkill, Long> {
    List<CompanyCareerSkill> findByCompanyIdAndCareerPathId(Long companyId, Long careerPathId);
    List<CompanyCareerSkill> findByCareerPathId(Long careerPathId);
    Optional<CompanyCareerSkill> findByCompanyIdAndCareerPathIdAndSkillId(Long companyId, Long careerPathId, Long skillId);
    boolean existsByCompanyIdAndCareerPathIdAndSkillId(Long companyId, Long careerPathId, Long skillId);
}
