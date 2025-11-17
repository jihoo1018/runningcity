package com.runningcity.report.repository;

import com.runningcity.run.entity.RunSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunSessionRepository extends JpaRepository<RunSession, Long> {

//    @Query(
//            value = """
//        SELECT * FROM run_session
//        WHERE user_id = :userId
//        AND end_time IS NOT NULL
//        """,
//            nativeQuery = true
//    )
    List<RunSession> findAllByUserIdAndEndTimeIsNotNull(Long userId);
}
