package com.runningcity.report.repository;

import com.runningcity.run.entity.RunSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<RunSession, Long> {

    @Query("""
  select rs from RunSession rs
  where rs.userId = :userId
    and rs.startTime >= :start
    and rs.startTime <  :end
  order by rs.startTime desc
""")
    List<RunSession> findMonthlySessions(
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("end")   Instant end
    );

    @Query("""
        select rs from RunSession rs
        where rs.sessionId = :sessionId
          and rs.userId    = :userId
          and rs.endTime is not null
    """)
    Optional<RunSession> findFinalizedByIdAndUserId(Long sessionId, Long userId);
}