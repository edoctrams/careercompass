package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;
import com.careercompass.careercompass.entity.InterviewExperience;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewExperienceRepository extends JpaRepository<InterviewExperience, Long> {
    List<InterviewExperience> findByCompanyId(Long companyId);
    List<InterviewExperience> findByUserId(Long userId);
}
