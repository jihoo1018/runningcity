package com.runningcity.mission.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public class RunSessionReadRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * run_session 테이블에서 오늘 뛴 거리 합계 가져오기
     */
    public double sumTodayDistance(Long userId, OffsetDateTime start, OffsetDateTime end) {
        String sql = """
                select coalesce(sum(rs.total_distance), 0)
                from run_session rs
                where rs.user_id = :userId
                  and rs.start_time >= :start
                  and rs.start_time < :end
                """;

        Object result = em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        return result == null ? 0.0 : ((Number) result).doubleValue();
    }
}
