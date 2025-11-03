package com.runningcity.run.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "run_session")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RunSession {

    @Id
    @Column(name = "session_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    // === 컬럼 매핑 (DDL 스네이크케이스와 1:1) ===
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", nullable = false)
    private String type; // NORMAL | INFILTRATION

    @Column(name = "base_id")
    private Long baseId;

    @Column(name = "device_type", nullable = false)
    private String deviceType; // ANDROID_PHONE | WEAR_OS

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE | CLOSING | FINALIZED

    @Column(name = "closing_deadline")
    private Instant closingDeadline;

    // 요약값
    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "distance_km", precision = 7, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "avg_pace_sec_per_km")
    private Integer avgPaceSecPerKm;

    @Column(name = "calories_kcal")
    private Integer caloriesKcal;

    @Column(name = "elevation_gain_m")
    private Integer elevationGainM;

    @Column(name = "avg_hr_bpm")
    private Short avgHrBpm;

    @Column(name = "avg_cadence_spm")
    private Short avgCadenceSpm;

    // JSONB 매핑 (내장 타입)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_meta")   // columnDefinition 생략 가능
    private Map<String, Object> resultMeta;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rewards_meta")
    private Map<String, Object> rewardsMeta;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // updated_at은 DB 트리거가 갱신 → 애플리케이션에서는 읽기 전용
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = "ACTIVE";
        if (type == null) type = "NORMAL";
    }
}
