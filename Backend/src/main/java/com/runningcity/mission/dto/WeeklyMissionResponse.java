// src/main/java/com/runningcity/mission/dto/WeeklyMissionResponse.java
package com.runningcity.mission.dto;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyMissionResponse {
    private Long missionId;
    private Long userId;
    private ZonedDateTime weekStart;
    private ZonedDateTime weekEnd;
    private int targetDays;
    private int completedDays;
    private int progressPercent;
    private boolean completed;
    private boolean claimed;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
