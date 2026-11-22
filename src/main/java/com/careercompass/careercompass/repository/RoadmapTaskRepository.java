package com.careercompass.careercompass.repository;

import java.util.List;
import java.util.Optional;
import com.careercompass.careercompass.entity.RoadmapTask;
import com.careercompass.careercompass.entity.RoadmapTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapTaskRepository extends JpaRepository<RoadmapTask, Long> {
    List<RoadmapTask> findByRoadmapId(Long roadmapId);
    List<RoadmapTask> findByRoadmapIdAndStatus(Long roadmapId, RoadmapTaskStatus status);
}
