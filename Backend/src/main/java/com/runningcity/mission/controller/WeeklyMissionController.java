// src/main/java/com/runningcity/mission/controller/WeeklyMissionController.java
package com.runningcity.mission.controller;

import com.runningcity.mission.dto.WeeklyMissionResponse;
import com.runningcity.mission.service.WeeklyMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/weekly-missions")
public class WeeklyMissionController {

    private final WeeklyMissionService weeklyMissionService;

    @GetMapping("/this-week")
    public WeeklyMissionResponse getThisWeek(@PathVariable Long userId) {
        return weeklyMissionService.getThisWeek(userId);
    }

    @PostMapping("/this-week/claim")
    public WeeklyMissionResponse claimThisWeek(@PathVariable Long userId) {
        return weeklyMissionService.claimThisWeek(userId);
    }
}
