package com.runningcity.run.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadPointsRequest {

    @NotNull(message = "points 배열은 비어 있을 수 없습니다.")
    @Size(min = 1, message = "points 배열은 최소 1개 이상이어야 합니다.")
    private List<@Valid Point> points;

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Point {
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

        // nullable
        private Double alt;          // m
        private Float  speedMps;     // m/s
        private Integer hrBpm;       // bpm
        private Integer cadenceSpm;  // steps/min
        private String  source;      // "PHONE" | "WATCH"
    }
}


