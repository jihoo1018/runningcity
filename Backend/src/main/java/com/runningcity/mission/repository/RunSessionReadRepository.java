// src/main/java/com/runningcity/mission/repository/RunSessionReadRepository.java
package com.runningcity.mission.repository;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.runningcity.run.entity.RunSession;

@Repository
public interface RunSessionReadRepository extends JpaRepository<RunSession, Long> {

    @Query(value = """
        select coalesce(sum(rs.total_distance), 0)
          from run_session rs
         where rs.user_id = :userId
           and rs.start_time >= :start
           and rs.start_time <  :end
        """, nativeQuery = true)
    double sumTodayDistance(@Param("userId") Long userId,
                            @Param("start") OffsetDateTime start,
                            @Param("end")   OffsetDateTime end);
}
