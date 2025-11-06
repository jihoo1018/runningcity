package com.runningcity.recordlist.repository;

import com.runningcity.recordlist.entity.MonthlyRunSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface MonthlyRunSessionRepository extends JpaRepository<MonthlyRunSession, Long> {

    @Query("select rs from MonthlyRunSession rs " +
           "where rs.userId = :userId and rs.startTime >= :start and rs.startTime < :end " +
           "order by rs.startTime desc")
    List<MonthlyRunSession> findMonthlySessions(
            @Param("userId") String userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}