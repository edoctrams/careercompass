package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;
import com.careercompass.careercompass.entity.CareerPath;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerPathRepository extends JpaRepository<CareerPath, Long> {
    Optional<CareerPath> findByName(String name);
}
