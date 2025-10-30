package com.runningcity.mission.controller;

import com.runningcity.global.common.ApiResponse;
import com.runningcity.global.common.CommonResponseCode;
import com.runningcity.mission.dto.DailyMissionProgressRequest;
import com.runningcity.mission.dto.DailyMissionResponse;
import com.runningcity.mission.service.DailyMissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 일일 미션 API (온보딩 컨벤션에 맞춤)
 */
@RestController
@RequestMapping("/users/{userId}/daily-missions")
@RequiredArgsConstructor
public class DailyMissionController {

    private final DailyMissionService dailyMissionService;

    /**
     * 오늘(서울 기준) 미션 조회.
     * 없으면 생성해서 내려준다.
     *
     * GET /users/{userId}/daily-missions/today
     */
    @GetMapping("/today")
    public ApiResponse<DailyMissionResponse> getTodayMission(
            @PathVariable("userId") Long userId
    ) {
        DailyMissionResponse response = dailyMissionService.getTodayMission(userId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, response);
    }

    /**
     * 오늘 미션 진행도 갱신
     *
     * PATCH /users/{userId}/daily-missions/today/progress
     */
    @PatchMapping("/today/progress")
    public ApiResponse<DailyMissionResponse> updateTodayProgress(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody DailyMissionProgressRequest request
    ) {
        DailyMissionResponse response = dailyMissionService.updateTodayProgress(userId, request);
        return ApiResponse.success(CommonResponseCode.SUCCESS, response);
    }

    /**
     * 보상 수령
     *
     * POST /users/{userId}/daily-missions/{missionId}/claim
     */
    @PostMapping("/{missionId}/claim")
    public ApiResponse<DailyMissionResponse> claimReward(
            @PathVariable("userId") Long userId,
            @PathVariable("missionId") Long missionId
    ) {
        DailyMissionResponse response = dailyMissionService.claim(userId, missionId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, response);
    }

    /**
     * (개발용) 오늘 미션 초기화
     *
     * POST /users/{userId}/daily-missions/reset
     */
    @PostMapping("/reset")
    public ApiResponse<Void> resetToday(
            @PathVariable("userId") Long userId
    ) {
        dailyMissionService.resetToday(userId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, null);
    }
}
