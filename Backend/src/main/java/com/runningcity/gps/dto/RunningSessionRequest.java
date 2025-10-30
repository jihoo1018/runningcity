package com.runningcity.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunningSessionRequest {
    private long startTime;
    private long endTime;
    private double totalDistance; // km
    private long duration; // ms
    private double avgPace; // min/km
    private double avgSpeed; // km/h
    private List<RunningPointRequest> route;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RunningPointRequest {
        private double latitude;
        private double longitude;
        private long timestamp;
    }
}
