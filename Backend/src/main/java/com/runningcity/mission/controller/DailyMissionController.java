package com.runningcity.mission.controller;

import com.runningcity.global.common.ApiResponse;
import com.runningcity.global.common.CommonResponseCode;
import com.runningcity.mission.dto.DailyMissionProgressRequest;
import com.runningcity.mission.dto.DailyMissionResponse;
import com.runningcity.mission.service.DailyMissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/daily-missions")
public class DailyMissionController {

    private final DailyMissionService dailyMissionService;

    public DailyMissionController(DailyMissionService dailyMissionService) {
        this.dailyMissionService = dailyMissionService;
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<DailyMissionResponse>> getTodayMission(@PathVariable Long userId) {
        DailyMissionResponse response = dailyMissionService.getTodayMission(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, response));
    }

    @PatchMapping("/today/progress")
    public ResponseEntity<ApiResponse<DailyMissionResponse>> addProgress(
            @PathVariable Long userId,
            @Valid @RequestBody DailyMissionProgressRequest request
    ) {
        DailyMissionResponse response = dailyMissionService.addProgress(userId, request);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, response));
    }

    @PostMapping("/{missionId}/claim")
    public ResponseEntity<ApiResponse<DailyMissionResponse>> claim(
            @PathVariable Long userId,
            @PathVariable Long missionId
    ) {
        DailyMissionResponse response = dailyMissionService.claim(userId, missionId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, response));
    }
}
