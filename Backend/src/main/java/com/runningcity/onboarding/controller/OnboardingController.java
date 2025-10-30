package com.runningcity.onboarding.controller;

import com.runningcity.onboarding.dto.OnboardingRequest;
import com.runningcity.onboarding.dto.OnboardingResponse;
import com.runningcity.onboarding.dto.OnboardingUpdateRequest;
import com.runningcity.onboarding.service.OnboardingService;
import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    /**
     * 온보딩 완료 API
     * @param userId 사용자 ID
     * @param request 온보딩 정보
     * @return 온보딩 완료 응답
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OnboardingResponse> completeOnboarding(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody OnboardingRequest request
            // TODO: JWT에서 userId 추출하여 PathVariable과 일치하는지 검증 필요
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        OnboardingResponse response = onboardingService.completeOnboarding(userId, request);
        
        return ApiResponse.success(
                CommonResponseCode.SUCCESS,
                response
        );
    }

    /**
     * 온보딩 완료 상태 조회 API
     * @param userId 사용자 ID
     * @return 온보딩 완료 여부
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Boolean> getOnboardingStatus(@PathVariable Long userId) {
        boolean isCompleted = onboardingService.isOnboardingCompleted(userId);
        
        return ApiResponse.success(
                CommonResponseCode.SUCCESS,
                isCompleted
        );
    }

    /**
     * 온보딩 정보 수정 API (목표 거리만 수정 가능)
     * @param userId 사용자 ID
     * @param request 수정할 온보딩 정보
     * @return 수정된 온보딩 정보
     */
    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OnboardingResponse> updateOnboarding(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody OnboardingUpdateRequest request
            // TODO: JWT에서 userId 추출하여 PathVariable과 일치하는지 검증 필요
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        OnboardingResponse response = onboardingService.updateOnboarding(userId, request);
        
        return ApiResponse.success(
                CommonResponseCode.SUCCESS,
                response
        );
    }
}

