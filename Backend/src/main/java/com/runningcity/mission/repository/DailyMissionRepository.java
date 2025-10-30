package com.runningcity.mission.repository;

import com.runningcity.mission.entity.DailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyMissionRepository extends JpaRepository<DailyMission, Long> {

    /**
     * 날짜 기준으로 미션을 찾는다.
     */
    Optional<DailyMission> findByDate(LocalDate date);
}
