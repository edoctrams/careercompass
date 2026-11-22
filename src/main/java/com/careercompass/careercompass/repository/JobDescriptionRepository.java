package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;
import com.careercompass.careercompass.entity.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findByUserId(Long userId);
}
