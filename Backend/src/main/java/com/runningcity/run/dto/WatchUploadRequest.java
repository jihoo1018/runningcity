package com.runningcity.run.dto;

import com.runningcity.run.dto.common.GpsPointLike;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WatchUploadRequest  {

    @NotBlank
    private String clientSecretKey;        // 멱등키(워치 생성 UUID)

    @Positive
    private long startTime;   // epoch millis

    @Positive
    private long endTime;     // epoch millis

    @Valid @NotNull
    private Summary summary;

    @Valid
    private List<CadenceRecord> cadenceRecords;

    @Valid
    private List<HeartRateRecord> heartRateRecords;

    @Valid
    private List<GpsPoint> gpsPoints;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Summary {
        @Min(0) private Integer totalSteps;
        @DecimalMin("0.0") private Double totalDistance; // meters
        @Min(0) private Integer totalCalories;           // kcal
        @Min(0) private Integer avgHeartRate;            // bpm
        @Min(0) private Integer duration;                // sec
        @Min(0) private Integer avgCadence;              // spm
        @Min(0) private Integer avgPace;                 // sec/k
        @DecimalMin("0.0") private Double elevation;     // meters
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CadenceRecord {
        @Positive int seq;
        @DecimalMin("0.0") double cadence;
        Long createdAt; // epoch millis
        // caloriesIncrement는 서버 저장 불필요 시 생략 가능 (DDL엔 주석만 있음)
        Double caloriesIncrement;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HeartRateRecord {
        @Positive int seq;
        @Min(0) int heartRate;
        Long createdAt; // epoch millis
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GpsPoint implements GpsPointLike {
        @Positive int seq;
        @DecimalMin("-90.0") @DecimalMax("90.0") double latitude;
        @DecimalMin("-180.0") @DecimalMax("180.0") double longitude;
        Double altitude; // nullable
        @DecimalMin("0.0") Double speed; // m/s, nullable
        @NotNull Long createdAt; // epoch millis
    }
}
