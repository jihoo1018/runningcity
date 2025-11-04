package com.runningcity.run.repository;

import com.runningcity.run.entity.RunSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunSessionRepository extends JpaRepository<RunSession, Long> {
}
