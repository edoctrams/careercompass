package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import com.careercompass.careercompass.entity.DailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyActivityRepository extends JpaRepository<DailyActivity, Long> {
    List<DailyActivity> findByUserId(Long userId);
    List<DailyActivity> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);
}
