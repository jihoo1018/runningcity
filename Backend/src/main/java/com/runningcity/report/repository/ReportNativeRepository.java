package com.runningcity.report.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ReportNativeRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * 5179 → 4326 변환 후 GeoJSON 반환
     * route_geom_simple 우선, 없으면 route_geom 사용
     */
    public Optional<String> findRouteGeoJson(Long sessionId) {
        String sql = """
            select ST_AsGeoJSON(
                     ST_Transform(
                       COALESCE(route_geom_simple, route_geom), 4326
                     )
                   )
            from run_route
            where session_id = ?1
            """;

        Object r = em.createNativeQuery(sql)
                .setParameter(1, sessionId)     // ← 포지셔널 파라미터
                .getResultStream()
                .findFirst()
                .orElse(null);

        return Optional.ofNullable(r)
                .map(Object::toString)
                .filter(s -> !s.isBlank());
    }
}
