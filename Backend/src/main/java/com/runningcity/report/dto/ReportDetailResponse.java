package com.runningcity.report.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDetailResponse {
    private String type;           // "NORMAL" | "ENTRY"
    private Summary summary;       // not null
    private Rewards rewards;       // nullable
    private Route route;           // nullable
    private AiReport aiReport;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Integer totalSteps;
        private Double  totalDistance;
        private Integer totalCalories;
        private Integer avgHeartRate;
        private Integer duration;
        private Integer avgCadence;
        private Integer avgPace;
        private Double  elevation;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Rewards {
        private Integer credit;    // nullable
        private Integer exp;       // nullable
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Route {
        private String geojson;    // nullable, GeoJSON LineString (WGS84)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiReport {
        private String content; // AI 리포트 내용
    }
}