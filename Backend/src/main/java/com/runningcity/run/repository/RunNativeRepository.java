package com.runningcity.run.repository;

import com.runningcity.run.dto.WatchUploadRequest;
import com.runningcity.run.dto.common.GpsPointLike;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RunNativeRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /** 모바일 시작: run_session INSERT → session_id 반환 */
    public long createSession(Long userId,
                              String type,
                              String deviceType,
                              Long baseId) {

        var p = new MapSqlParameterSource()
                .addValue("uid", userId)
                .addValue("type", type)
                .addValue("device", deviceType)
                .addValue("baseId", baseId);

        String sql = """
        INSERT INTO run_session (
            user_id, type, base_id, device_type
        )
        VALUES (
            :uid, :type, :baseId, :device
        )
        RETURNING session_id
        """;

        return jdbc.queryForObject(sql, p, Long.class);
    }


    /** run_session upsert + summary/json 저장 → session_id 반환 */
    /** run_session upsert + summary/json 저장 → session_id 반환 */
    public long upsertSessionAndSummary(
            String clientSecretKey,
            Long userId,
            Instant startTime,
            Instant endTime,
            String type,
            String deviceType,
            Long baseId,
            Integer totalSteps,
            Double totalDistance,
            Integer totalCalories,
            Integer avgHeartRate,
            Integer duration,
            Integer avgCadence,
            Integer avgPace,
            Double elevation,
            String cadenceJson,
            String hrJson
    ) {
        // null-safe timestamptz 변환
        OffsetDateTime st = (startTime == null) ? null : OffsetDateTime.ofInstant(startTime, ZoneOffset.UTC);
        OffsetDateTime et = (endTime   == null) ? null : OffsetDateTime.ofInstant(endTime,   ZoneOffset.UTC);

        var p = new MapSqlParameterSource()
                .addValue("clientKey", clientSecretKey)
                .addValue("uid", userId)
                .addValue("st", st)
                .addValue("et", et)
                .addValue("type", type)
                .addValue("device", deviceType)
                .addValue("baseId", baseId)
                .addValue("steps", totalSteps)
                .addValue("dist", totalDistance)
                .addValue("cal", totalCalories)
                .addValue("ahr", avgHeartRate)
                .addValue("dur", duration)
                .addValue("cad", avgCadence)
                .addValue("pace", avgPace)
                .addValue("elev", elevation)
                // JSON 파라미터는 타입 명시 (NULL이어도 OK)
                .addValue("cadJson", cadenceJson, java.sql.Types.OTHER)
                .addValue("hrJson",  hrJson,      java.sql.Types.OTHER);

            String sql = """
            INSERT INTO run_session (
                user_id, client_secret_key, type, base_id, device_type,
                start_time, end_time,
                total_steps, total_distance, total_calories, avg_heart_rate,
                duration, avg_cadence, avg_pace, elevation,
                cadence_records, heart_rate_records
            )
            VALUES (
                :uid, :clientKey, :type, :baseId, :device,
                :st, :et,
                :steps, :dist, :cal, :ahr,
                :dur, :cad, :pace, :elev,
                :cadJson::jsonb, :hrJson::jsonb
            )
            ON CONFLICT (user_id, client_secret_key)
            DO UPDATE SET
                -- start_time: 더 이르면 교체 (NULL도 허용)
                start_time = CASE
                               WHEN EXCLUDED.start_time IS NOT NULL
                                    AND (run_session.start_time IS NULL OR EXCLUDED.start_time < run_session.start_time)
                                 THEN EXCLUDED.start_time
                               ELSE run_session.start_time
                             END,
                -- end_time: 체크 제약(end_time >= start_time) 안 깨지게 보정
                end_time = GREATEST(
                             EXCLUDED.end_time,
                             COALESCE(run_session.start_time, EXCLUDED.start_time)
                           ),
                type = EXCLUDED.type,
                base_id = EXCLUDED.base_id,
                device_type = EXCLUDED.device_type,
                total_steps = EXCLUDED.total_steps,
                total_distance = EXCLUDED.total_distance,
                total_calories = EXCLUDED.total_calories,
                avg_heart_rate = EXCLUDED.avg_heart_rate,
                duration = EXCLUDED.duration,
                avg_cadence = EXCLUDED.avg_cadence,
                avg_pace = EXCLUDED.avg_pace,
                elevation = EXCLUDED.elevation,
                cadence_records = EXCLUDED.cadence_records,
                heart_rate_records = EXCLUDED.heart_rate_records
            RETURNING session_id
        """;

        return jdbc.queryForObject(sql, p, Long.class);
    }


    /** gps_points 대량 멱등 삽입 (4326 → 5179 PointZ) — 공통 GpsPointLike 사용 */
    public int[] batchInsertPoints(long sid, java.util.Collection<? extends GpsPointLike> points) {
        if (points == null || points.isEmpty()) {
            return new int[0];
        }

        String sql = """
        INSERT INTO gps_points (session_id, seq, created_at, geom, speed)
        VALUES (
            :sid, :seq, :ts,
            ST_Transform(ST_SetSRID(ST_MakePoint(:lon, :lat, :alt), 4326), 5179),
            :spd
        )
        ON CONFLICT (session_id, seq) DO NOTHING
        """;

        var batch = new java.util.ArrayList<org.springframework.jdbc.core.namedparam.MapSqlParameterSource>(points.size());
        for (var p : points) {
            Double alt = p.getAltitude() == null ? 0.0 : p.getAltitude();
            batch.add(new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                    .addValue("sid", sid)
                    .addValue("seq", p.getSeq())
                    // epoch millis -> Instant -> OffsetDateTime(UTC)로 변환 (timestamptz 안전)
                    .addValue("ts", java.time.OffsetDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(p.getCreatedAt()), java.time.ZoneOffset.UTC))
                    .addValue("lon", p.getLongitude())
                    .addValue("lat", p.getLatitude())
                    .addValue("alt", alt)
                    .addValue("spd", p.getSpeed()));
        }
        return jdbc.batchUpdate(sql, batch.toArray(org.springframework.jdbc.core.namedparam.MapSqlParameterSource[]::new));
    }


    /** 경로 라인/단순화/길이 upsert (INSERT 기본값/UPDATE 트리거 사용) */
    public boolean upsertRouteAndLength(long sid, double simplifyTolMeters) {
        var p = new MapSqlParameterSource()
                .addValue("sid", sid)
                .addValue("tol", simplifyTolMeters);

        String sql = """
            WITH pts AS (
              SELECT geom FROM gps_points
              WHERE session_id = :sid
              ORDER BY seq
            ),
            npts AS ( SELECT COUNT(*) AS n FROM pts ),
            line_raw AS (
              SELECT CASE WHEN (SELECT n FROM npts) >= 2
                     THEN ST_MakeLine(geom) ELSE NULL END AS g
              FROM pts LIMIT 1
            ),
            cleaned AS (
              SELECT CASE WHEN g IS NOT NULL THEN ST_RemoveRepeatedPoints(g, 0.5) END AS g
              FROM line_raw
            ),
            simplified AS (
              SELECT CASE WHEN g IS NOT NULL THEN ST_SimplifyVW(g, :tol) END AS g
              FROM cleaned
            ),
            lens AS (
              SELECT CASE WHEN (SELECT g FROM cleaned) IS NOT NULL
                     THEN ST_Length((SELECT g FROM cleaned)) END AS len
            )
            INSERT INTO run_route (session_id, route_geom, route_geom_simple, length_m)
            SELECT :sid, (SELECT g FROM cleaned), (SELECT g FROM simplified), (SELECT len FROM lens)
            ON CONFLICT (session_id) DO UPDATE SET
              route_geom = EXCLUDED.route_geom,
              route_geom_simple = EXCLUDED.route_geom_simple,
              length_m = EXCLUDED.length_m
        """;

        int n = jdbc.update(sql, p);
        return n > 0;
    }



    /** Finish: 요약/JSON/종료시간 업데이트 + rewards 최초 1회 저장 + users 누적 1회 가산
     *  - 서비스는 Instant만 사용 → 여기서만 OffsetDateTime(UTC)로 변환
     */
    // RunNativeRepository.java
    /** Finish: 요약/JSON/종료시간 업데이트 + rewards 최초 1회 저장 + users 누적 1회 가산 */
    public int finishUpdateSessionAndMaybeApplyRewards(
            long sid,
            Instant startTime,
            Instant endTime,
            Integer totalSteps, Double totalDistance, Integer totalCalories,
            Integer avgHeartRate, Integer duration, Integer avgCadence, Integer avgPace, Double elevation,
            String cadenceJson, String hrJson,
            String rewardsJson,
            String clientKey
    ) {
        // null-safe timestamptz 변환
        OffsetDateTime st = (startTime == null) ? null : OffsetDateTime.ofInstant(startTime, ZoneOffset.UTC);
        OffsetDateTime et = (endTime   == null) ? null : OffsetDateTime.ofInstant(endTime,   ZoneOffset.UTC);

        boolean hasRewards = (rewardsJson != null);

        var p = new MapSqlParameterSource()
                .addValue("sid", sid)
                .addValue("st", st)
                .addValue("et", et)
                .addValue("steps", totalSteps)
                .addValue("dist", totalDistance)
                .addValue("cal", totalCalories)
                .addValue("ahr", avgHeartRate)
                .addValue("dur", duration)
                .addValue("cad", avgCadence)
                .addValue("pace", avgPace)
                .addValue("elev", elevation)
                // JSON 파라미터는 Types.OTHER로 명시 (null 허용)
                .addValue("cadJson",     cadenceJson, java.sql.Types.OTHER)
                .addValue("hrJson",      hrJson,      java.sql.Types.OTHER)
                .addValue("rewardsJson", rewardsJson, java.sql.Types.OTHER)
                .addValue("hasRewards",  hasRewards)
                .addValue("clientKey",   clientKey);

        String sql = """
        WITH sel AS (
          SELECT user_id, start_time AS st0, rewards_meta
          FROM run_session
          WHERE session_id = :sid
          FOR UPDATE
        ),
        upd AS (
          UPDATE run_session rs
          SET
            -- 요청 startTime이 더 이르면 교체 (NULL 방지)
            start_time = CASE
                           WHEN :st IS NOT NULL
                                AND (rs.start_time IS NULL OR :st < rs.start_time)
                             THEN :st
                           ELSE rs.start_time
                         END,
            -- end_time: 제약(end_time >= start_time) 보정 + et가 NULL일 때 rs.end_time 유지
            end_time   = GREATEST(
                           COALESCE(:et, rs.end_time),
                           CASE
                             WHEN :st IS NOT NULL
                                  AND (rs.start_time IS NULL OR :st < rs.start_time)
                               THEN :st
                             ELSE rs.start_time
                           END
                         ),
            total_steps        = :steps,
            total_distance     = :dist,
            total_calories     = :cal,
            avg_heart_rate     = :ahr,
            duration           = :dur,
            avg_cadence        = :cad,
            avg_pace           = :pace,
            elevation          = :elev,
            cadence_records    = :cadJson::jsonb,
            heart_rate_records = :hrJson::jsonb,
            client_secret_key  = CASE
                                   WHEN :clientKey IS NOT NULL AND rs.client_secret_key IS NULL
                                     THEN :clientKey
                                   ELSE rs.client_secret_key
                                 END,
            -- 보상은 최초 1회만 기록 (NULL 비교 대신 hasRewards 사용)
            rewards_meta       = CASE
                                   WHEN :hasRewards = FALSE THEN rs.rewards_meta
                                   WHEN (SELECT rewards_meta FROM sel) IS NULL AND :hasRewards = TRUE
                                     THEN :rewardsJson::jsonb
                                   ELSE rs.rewards_meta
                                 END
          FROM sel
          WHERE rs.session_id = :sid
          RETURNING
            (SELECT user_id FROM sel) AS user_id,
            -- 최초 적용 여부: 기존 rewards_meta가 NULL이고 이번 요청에 보상이 있을 때
            ((SELECT rewards_meta FROM sel) IS NULL AND :hasRewards = TRUE) AS will_apply,
            -- 최종 반영될 rewards
            COALESCE(
              CASE
                WHEN (SELECT rewards_meta FROM sel) IS NULL AND :hasRewards = TRUE
                  THEN :rewardsJson::jsonb
                ELSE (SELECT rewards_meta FROM sel)
              END,
              rs.rewards_meta
            ) AS final_rewards
        ),
        apply AS (
          UPDATE users u
          SET total_exp    = u.total_exp
                              + COALESCE((upd.final_rewards->>'exp')::bigint, 0),
              total_credit = u.total_credit
                              + COALESCE((upd.final_rewards->>'credit')::bigint, 0)
          FROM upd
          WHERE upd.will_apply = true
            AND u.user_id = upd.user_id
          RETURNING 1
        )
        SELECT COALESCE((SELECT 1 FROM apply LIMIT 1), 0) AS applied
    """;

        Integer applied = jdbc.queryForObject(sql, p, Integer.class);
        return applied == null ? 0 : applied; // 1이면 이번에 최초 가산됨
    }

}
