package com.runningcity.mission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMissionResponse {

    @JsonProperty("mission_id")
    private Long missionId;

    @JsonProperty("user_id")
    private Long userId;

    private ZonedDateTime missionDate;

    private double targetKm;
    private double currentKm;
    private int progressPercent;
    private boolean completed;
    private boolean claimed;

    private ZonedDateTime updatedAt;
}
