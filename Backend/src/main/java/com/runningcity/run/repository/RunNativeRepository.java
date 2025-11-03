package com.runningcity.run.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class RunNativeRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /** 세션 업로드 상태 행 생성 (없으면 생성) */
    public void insertSessionUploadRow(long sid){
        jdbc.update("""
            INSERT INTO run_session_upload(session_id, acked_until_seq)
            VALUES (:sid, 0)
            ON CONFLICT (session_id) DO NOTHING
        """, Map.of("sid", sid));
    }

    /** run_point 대량 멱등 insert (4326 → 5179 변환) */
    public int[] batchInsertPoints(long sid, List<Map<String, Object>> params){
        return jdbc.batchUpdate("""
            INSERT INTO run_point(session_id, seq, recorded_at, geom, speed_mps, hr_bpm, cadence_spm, source)
            VALUES(
              :sid, :seq, :ts,
              ST_Transform(ST_SetSRID(ST_MakePoint(:lon, :lat, COALESCE(:alt,0)), 4326), 5179),
              :spd, :hr, :cad, :src
            )
            ON CONFLICT (session_id, seq) DO NOTHING
        """, params.stream().map(m -> {
            Map<String,Object> p = new HashMap<>(m);
            p.put("sid", sid);
            Instant ts = (Instant) m.get("ts");
            OffsetDateTime odt = (ts == null) ? null : OffsetDateTime.ofInstant(ts, ZoneOffset.UTC);
            p.put("ts", odt);
            return p;
        }).toArray(Map[]::new));
    }

    /** 연속 ACK 갱신 후 최신 acked_until_seq 반환 (FOR UPDATE) */
    public Integer updateAckAndReturn(long sid, int batchMaxSeq){
        Map<String,Object> base = Map.of("sid", sid, "bmax", batchMaxSeq);

        Integer ack = jdbc.queryForObject("""
            SELECT acked_until_seq
            FROM run_session_upload
            WHERE session_id = :sid
            FOR UPDATE
        """, base, Integer.class);

        Map<String,Object> p = new HashMap<>(base);
        p.put("ack", ack);

        return jdbc.queryForObject("""
            WITH miss AS (
              SELECT s
              FROM generate_series(:ack + 1, :bmax) AS g(s)
              LEFT JOIN run_point p
                ON p.session_id = :sid AND p.seq = g.s
              WHERE p.seq IS NULL
              ORDER BY s
              LIMIT 1
            )
            UPDATE run_session_upload
            SET acked_until_seq = CASE
              WHEN EXISTS (SELECT 1 FROM miss)
                THEN (SELECT MIN(s) - 1 FROM miss)
              ELSE GREATEST(acked_until_seq, :bmax)
            END
            WHERE session_id = :sid
            RETURNING acked_until_seq
        """, p, Integer.class);
    }

    /** 현재 ACK 조회 */
    public Integer getAck(long sid){
        return jdbc.queryForObject("""
            SELECT acked_until_seq
            FROM run_session_upload
            WHERE session_id = :sid
        """, Map.of("sid", sid), Integer.class);
    }

    /**
     * 라인 생성/단순화/길이 UPSERT
     * - 포인트 < 2개면 0행 INSERT (NOT NULL 위반 없음)
     * - 컬럼명: route_geom, route_geom_simple, length_m
     */
    public void upsertRouteAndLength(long sid, double simplifyTolMeters){
        jdbc.update("""
            WITH pts AS (
              SELECT geom
              FROM run_point
              WHERE session_id = :sid
              ORDER BY seq
            ),
            line_raw AS ( SELECT ST_MakeLine(geom) AS g FROM pts ),
            line_clean AS ( SELECT ST_RemoveRepeatedPoints(g, 0.5) AS g FROM line_raw ),
            ok AS (
              SELECT g FROM line_clean
              WHERE g IS NOT NULL AND ST_NPoints(g) >= 2
            ),
            snapped AS ( SELECT ST_SnapToGrid(g, 0.1) AS g FROM ok ),
            simp AS ( SELECT ST_SimplifyPreserveTopology(g, :tol) AS s FROM snapped ),
            len  AS ( SELECT ST_Length(g) AS meters FROM snapped )
            INSERT INTO run_route (session_id, route_geom, route_geom_simple, length_m, updated_at)
            SELECT :sid, snapped.g, simp.s, len.meters, now()
            FROM snapped
            JOIN simp ON true
            JOIN len  ON true
            ON CONFLICT (session_id) DO UPDATE
              SET route_geom        = EXCLUDED.route_geom,
                  route_geom_simple = EXCLUDED.route_geom_simple,
                  length_m          = EXCLUDED.length_m,
                  updated_at        = now()
        """, Map.of("sid", sid, "tol", simplifyTolMeters));
    }


    /** [ADDED] FINALIZED 전 요약 PATCH (null은 무시) */
    public void upsertSummaryWhileOpen(long sid, Map<String, Object> summary) {
        MapSqlParameterSource p = new MapSqlParameterSource(summary).addValue("sid", sid);
        jdbc.update("""
            UPDATE run_session
            SET duration_sec           = COALESCE(:durationSec,        duration_sec),
                distance_km            = COALESCE(:distanceKm,         distance_km),
                avg_pace_sec_per_km    = COALESCE(:avgPaceSecPerKm,    avg_pace_sec_per_km),
                calories_kcal          = COALESCE(:caloriesKcal,       calories_kcal),
                elevation_gain_m       = COALESCE(:elevationGainM,     elevation_gain_m),
                avg_hr_bpm             = COALESCE(:avgHrBpm,           avg_hr_bpm),
                avg_cadence_spm        = COALESCE(:avgCadenceSpm,      avg_cadence_spm)
            WHERE session_id = :sid
              AND status <> 'FINALIZED'
        """, p);
    }

    /** CLOSING 최초 전환(연장 금지) — run_session.session_id 기준 */
    public void setClosingIfFirstTime(long sid, int seconds){
        jdbc.update("""
            UPDATE run_session
            SET status = 'CLOSING',
                closing_deadline = now() + make_interval(secs => :sec)
            WHERE session_id = :sid
              AND status = 'ACTIVE'
              AND closing_deadline IS NULL
        """, Map.of("sid", sid, "sec", seconds));
    }

    /** FINALIZED 세션 요약 반영 + end_at 갱신 — run_session.session_id 기준 */
    public void finalizeSession(long sid, Map<String, Object> summary){
        MapSqlParameterSource p = new MapSqlParameterSource(summary).addValue("sid", sid);
        jdbc.update("""
            UPDATE run_session
            SET status='FINALIZED',
                end_at=now(),
                duration_sec=:durationSec,
                distance_km=:distanceKm,
                avg_pace_sec_per_km=:avgPaceSecPerKm,
                calories_kcal=:caloriesKcal,
                elevation_gain_m=:elevationGainM,
                avg_hr_bpm=:avgHrBpm,
                avg_cadence_spm=:avgCadenceSpm
            WHERE session_id = :sid
        """, p);
    }

    /** 상태 단건 조회 — run_session.session_id 기준 */
    public String getStatus(long sid){
        return jdbc.queryForObject("""
            SELECT status
            FROM run_session
            WHERE session_id = :sid
        """, Map.of("sid", sid), String.class);
    }

    /** FINALIZED 시각 조회(없으면 null) — run_session.session_id 기준 */
    public Instant getFinalizedAtOrNull(long sid){
        return jdbc.query("""
            SELECT end_at
            FROM run_session
            WHERE session_id = :sid
        """, Map.of("sid", sid), rs -> {
            if (!rs.next()) return null;
            var ts = rs.getTimestamp(1);
            return ts != null ? ts.toInstant() : null;
        });
    }
}
