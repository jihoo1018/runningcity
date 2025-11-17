// src/main/java/com/runningcity/mission/repository/WeeklyMissionRepository.java
package com.runningcity.mission.repository;

import com.runningcity.mission.entity.WeeklyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface WeeklyMissionRepository extends JpaRepository<WeeklyMission, Long> {
    Optional<WeeklyMission> findByUserIdAndWeekStart(Long userId, ZonedDateTime weekStart);
}
