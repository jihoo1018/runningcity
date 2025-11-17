package com.runningcity.report.repository;

import com.runningcity.run.entity.RunSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunSessionRepository extends JpaRepository<RunSession, Long> {
}
