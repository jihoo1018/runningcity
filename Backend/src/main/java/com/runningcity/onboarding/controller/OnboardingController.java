package com.runningcity.onboarding.controller;

import com.runningcity.onboarding.dto.OnboardingRequest;
import com.runningcity.onboarding.dto.OnboardingResponse;
import com.runningcity.onboarding.service.OnboardingService;
import com.runningcity.global.common.ApiResponse;
import com.runningcity.global.common.CommonResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    /**
     * 온보딩 완료 API
     * @param request 온보딩 정보
     * @return 온보딩 완료 응답
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OnboardingResponse> completeOnboarding(
            @Valid @RequestBody OnboardingRequest request
            // TODO: JWT에서 userId 추출하는 로직 추가 필요
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 임시로 userId를 1L로 설정 (추후 JWT에서 추출하도록 수정)
        Long userId = 1L; // TODO: JWT에서 실제 userId 추출
        
        OnboardingResponse response = onboardingService.completeOnboarding(userId, request);
        
        return ApiResponse.success(
                CommonResponseCode.SUCCESS,
                response
        );
    }

    /**
     * 온보딩 완료 상태 조회 API
     * @return 온보딩 완료 여부
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Boolean> getOnboardingStatus() {
        // 임시로 userId를 1L로 설정 (추후 JWT에서 추출하도록 수정)
        Long userId = 1L; // TODO: JWT에서 실제 userId 추출
        
        boolean isCompleted = onboardingService.isOnboardingCompleted(userId);
        
        return ApiResponse.success(
                CommonResponseCode.SUCCESS,
                isCompleted
        );
    }
}

