package com.runningcity.run.repository;

import com.runningcity.run.dto.WatchUploadRequest;
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
                user_id, type, base_id, device_type, start_time
            )
            VALUES (
                :uid, :type, :baseId, :device, now()
            )
            RETURNING session_id
            """;

        return jdbc.queryForObject(sql, p, Long.class);
    }

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
        var p = new MapSqlParameterSource()
                .addValue("clientKey", clientSecretKey)
                .addValue("uid", userId)
                // Instant -> OffsetDateTime(UTC)로 변환하여 timestamptz에 바인딩
                .addValue("st", OffsetDateTime.ofInstant(startTime, ZoneOffset.UTC))
                .addValue("et", OffsetDateTime.ofInstant(endTime, ZoneOffset.UTC))
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
                .addValue("cadJson", cadenceJson)
                .addValue("hrJson", hrJson);

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
                CAST(:cadJson AS jsonb), CAST(:hrJson AS jsonb)
            )
            ON CONFLICT (client_secret_key, user_id, start_time)
            DO UPDATE SET
                end_time = EXCLUDED.end_time,
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

    /** gps_points 대량 멱등 삽입 (4326 → 5179 PointZ) */
    public int[] batchInsertPoints(long sid, List<WatchUploadRequest.GpsPoint> points) {
        String sql = """
            INSERT INTO gps_points (session_id, seq, created_at, geom, speed)
            VALUES (
                :sid, :seq, :ts,
                ST_Transform(ST_SetSRID(ST_MakePoint(:lon, :lat, :alt), 4326), 5179),
                :spd
            )
            ON CONFLICT (session_id, seq) DO NOTHING
        """;

        var batch = new ArrayList<MapSqlParameterSource>(points.size());
        for (var p : points) {
            Double alt = p.getAltitude() == null ? 0.0 : p.getAltitude();
            batch.add(new MapSqlParameterSource()
                    .addValue("sid", sid)
                    .addValue("seq", p.getSeq())
                    // epoch millis -> Instant -> OffsetDateTime(UTC)로 변환
                    .addValue("ts", OffsetDateTime.ofInstant(Instant.ofEpochMilli(p.getCreatedAt()), ZoneOffset.UTC))
                    .addValue("lon", p.getLongitude())
                    .addValue("lat", p.getLatitude())
                    .addValue("alt", alt)
                    .addValue("spd", p.getSpeed()));
        }
        return jdbc.batchUpdate(sql, batch.toArray(MapSqlParameterSource[]::new));
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
}
