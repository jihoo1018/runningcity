package com.runningcity.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.runningcity.report.entity.Report;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("select rs from Report rs " +
           "where rs.userId = :userId and rs.startTime >= :start and rs.startTime < :end " +
           "order by rs.startTime desc")
    List<Report> findMonthlySessions(
            @Param("userId") String userId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}