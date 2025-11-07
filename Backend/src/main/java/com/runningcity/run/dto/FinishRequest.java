// src/main/java/com/runningcity/run/dto/FinishRequest.java
package com.runningcity.run.dto;

import com.runningcity.run.dto.common.GpsPointLike;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinishRequest {

    private String clientSecretKey;

    @Positive private long startTime;
    @Positive private long endTime;

    @Valid @NotNull
    private Summary summary;

    @Valid private List<HeartRateRecord> heartRateRecords;
    @Valid private List<CadenceRecord> cadenceRecords;

    @Valid @NotEmpty
    private List<GpsPoint> gpsPoints;

    // OPTIONAL: ENTRY(잡입)일 때만 내려올 수도 있음
    @Valid
    private Rewards rewards; // null 가능

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Summary {
        @Min(0) private Integer totalSteps;
        @DecimalMin("0.0") private Double totalDistance; // meters
        @Min(0) private Integer totalCalories;
        @Min(0) private Integer avgHeartRate;
        @Min(0) private Integer duration;   // sec
        @Min(0) private Integer avgCadence; // spm
        @Min(0) private Integer avgPace;    // sec/km
        @DecimalMin("0.0") private Double elevation;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CadenceRecord {
        @Positive int seq;
        @DecimalMin("0.0") double cadence;
        Long createdAt;
        Double caloriesIncrement;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HeartRateRecord {
        @Positive int seq;
        @Min(0) int heartRate;
        Long createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GpsPoint implements GpsPointLike {
        @Positive int seq;
        @DecimalMin("-90.0")  @DecimalMax("90.0")   double latitude;
        @DecimalMin("-180.0") @DecimalMax("180.0")  double longitude;
        Double altitude;
        @DecimalMin("0.0") Double speed;
        @NotNull Long createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Rewards {
        @Min(0) private Long exp;
        @Min(0) private Long credit;
    }
}
