package com.runningcity.run.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
@Table(name = "run_session")
public class RunSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "client_secret_key")
    private String clientSecretKey;

    @Column(name = "type", nullable = false)
    private String type; // NORMAL | ENTRY

    @Column(name = "base_id")
    private Long baseId;

    @Column(name = "device_type", nullable = false)
    private String deviceType; // PHONE | WATCH

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    // summary (읽기용 필드들)
    @Column(name = "total_steps")     private Integer totalSteps;
    @Column(name = "total_distance")  private Double totalDistance;
    @Column(name = "total_calories")  private Integer totalCalories;
    @Column(name = "avg_heart_rate")  private Integer avgHeartRate;
    @Column(name = "duration")        private Integer duration;
    @Column(name = "avg_cadence")     private Integer avgCadence;
    @Column(name = "avg_pace")        private Integer avgPace;
    @Column(name = "elevation")       private Double elevation;

    // JSONB (읽기 전용)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cadence_records", columnDefinition = "jsonb")
    private JsonNode cadenceRecords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "heart_rate_records", columnDefinition = "jsonb")
    private JsonNode heartRateRecords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rewards_meta", columnDefinition = "jsonb")
    private JsonNode rewardsMeta;

    // DB/네이티브가 채움 (JPA는 단순 매핑)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
