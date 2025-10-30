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
@RequestMapping("/users/{userId}/daily-missions")
public class DailyMissionController {

    private final DailyMissionService dailyMissionService;

    public DailyMissionController(DailyMissionService dailyMissionService) {
        this.dailyMissionService = dailyMissionService;
    }

    /**
     * 오늘 일일 미션 조회
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<DailyMissionResponse>> getTodayMission(@PathVariable Long userId) {
        DailyMissionResponse response = dailyMissionService.getTodayMission(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, response));
    }

    /**
     * 오늘 미션 진행도 추가
     */
    @PatchMapping("/today/progress")
    public ResponseEntity<ApiResponse<DailyMissionResponse>> addProgress(
            @PathVariable Long userId,
            @Valid @RequestBody DailyMissionProgressRequest request
    ) {
        DailyMissionResponse response = dailyMissionService.addProgress(userId, request);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, response));
    }

    /**
     * (기존) 특정 미션 ID로 보상 수령
     * 프론트가 /{missionId}/claim 쓰고 있으면 이거 그대로 둔다.
     */
    @PostMapping("/{missionId}/claim")
    public ResponseEntity<ApiResponse<DailyMissionResponse>> claim(
            @PathVariable Long userId,
            @PathVariable Long missionId
    ) {
        DailyMissionResponse response = dailyMissionService.claim(userId, missionId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, response));
    }

    /**
     * ✅ (추가) 오늘 미션 보상 수령
     * 지금 프론트는 이 경로를 쓰고 있으니까 이걸 꼭 넣어줘야 한다.
     * POST /api/v1/users/{userId}/daily-missions/today/claim
     */
    @PostMapping("/today/claim")
    public ResponseEntity<ApiResponse<DailyMissionResponse>> claimToday(@PathVariable Long userId) {
        DailyMissionResponse response = dailyMissionService.claimToday(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, response));
    }

    /**
     * ✅ (복구) 오늘 미션 초기화 - 개발/테스트용
     * POST /api/v1/users/{userId}/daily-missions/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetToday(@PathVariable Long userId) {
        dailyMissionService.resetToday(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, null));
    }
}
