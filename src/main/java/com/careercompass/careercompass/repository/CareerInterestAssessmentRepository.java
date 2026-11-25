
package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careercompass.careercompass.entity.CareerInterestAssessment;

public interface CareerInterestAssessmentRepository
        extends JpaRepository<CareerInterestAssessment, Long> {

    // Retrieve all assessments belonging to a student.
    List<CareerInterestAssessment> findAllByUser_IdOrderByCreatedAtDesc(
            Long userId
    );

    // Retrieve the student's most recent assessment.
    Optional<CareerInterestAssessment> findTopByUser_IdOrderByCreatedAtDesc(
            Long userId
    );
}
