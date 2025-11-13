package com.runningcity.mission.controller;

import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.mission.dto.DailyMissionResponse;
import com.runningcity.mission.service.DailyMissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/daily-missions")
public class DailyMissionController {

    private final DailyMissionService dailyMissionService;

    public DailyMissionController(DailyMissionService dailyMissionService) {
        this.dailyMissionService = dailyMissionService;
    }

    /**
     * 오늘 일일 미션 조회
     * GET /api/v1/users/{userId}/daily-missions/today
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<DailyMissionResponse>> getTodayMission(@PathVariable Long userId) {
        DailyMissionResponse response = dailyMissionService.getTodayMission(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, response));
    }

    /**
     * 오늘 일일 미션 보상 수령
     * POST /api/v1/users/{userId}/daily-missions/today/claim
     */
    @PostMapping("/today/claim")
    public ResponseEntity<ApiResponse<DailyMissionResponse>> claimToday(@PathVariable Long userId) {
        DailyMissionResponse response = dailyMissionService.claimToday(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, response));
    }

    /**
     * (테스트용) 오늘 미션 초기화
     * POST /api/v1/users/{userId}/daily-missions/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetToday(@PathVariable Long userId) {
        dailyMissionService.resetToday(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, null));
    }
}
