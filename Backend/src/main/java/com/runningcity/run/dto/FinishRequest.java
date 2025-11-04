package com.runningcity.run.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinishRequest {

    @NotNull(message = "deviceSummary는 필수입니다.")
    @Valid
    private DeviceSummary deviceSummary;

    @NotNull @Positive(message = "clientLastSeq는 1 이상이어야 합니다.")
    private Integer clientLastSeq;

    private List<@Valid TailPoint> tailPoints;

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DeviceSummary {
        @NotNull @DecimalMin(value = "0.0", message = "distanceKm는 0 이상이어야 합니다.")
        private Double distanceKm;

        @NotNull @PositiveOrZero(message = "durationSec는 0 이상이어야 합니다.")
        private Integer durationSec;

        @PositiveOrZero(message = "avgPaceSecPerKm는 0 이상이어야 합니다.")
        private Integer avgPaceSecPerKm;

        @PositiveOrZero(message = "caloriesKcal는 0 이상이어야 합니다.")
        private Integer caloriesKcal;

        private Integer elevationGainM;
        private Integer avgHrBpm;
        private Integer avgCadenceSpm;
    }

    // 남은 애들 같이 보내기
    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TailPoint {
        @NotNull @Positive(message = "seq는 1 이상이어야 합니다.")
        private Integer seq;

        @NotNull(message = "recordedAt(UTC)은 필수입니다.")
        private Instant recordedAt;

        @NotNull @DecimalMin(value = "-180.0", message = "경도는 -180.0 이상 180.0 이하여야 합니다.")
        @DecimalMax(value = "180.0",  message = "경도는 -180.0 이상 180.0 이하여야 합니다.")
        private Double lon;

        @NotNull @DecimalMin(value = "-90.0", message = "위도는 -90.0 이상 90.0 이하여야 합니다.")
        @DecimalMax(value = "90.0",  message = "위도는 -90.0 이상 90.0 이하여야 합니다.")
        private Double lat;

        private Double alt;
        private Float  speedMps;
        private Integer hrBpm;
        private Integer cadenceSpm;
        private String  source; // "PHONE" | "WATCH"
    }
}


