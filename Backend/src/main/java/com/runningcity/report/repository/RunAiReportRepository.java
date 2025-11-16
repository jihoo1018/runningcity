package com.runningcity.report.repository;

import com.runningcity.report.entity.RunAiReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RunAiReportRepository extends JpaRepository<RunAiReport, Long> {
    Optional<RunAiReport> findByRunSession_SessionId(Long sessionId);
}
