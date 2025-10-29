package com.runningcity.onboarding.service;

import com.runningcity.onboarding.dto.OnboardingRequest;
import com.runningcity.onboarding.dto.OnboardingResponse;
import com.runningcity.onboarding.entity.UserPreference;
import com.runningcity.onboarding.repository.UserPreferenceRepository;
import com.runningcity.user.entity.User;
import com.runningcity.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    /**
     * 사용자 온보딩 완료 처리
     * @param userId JWT에서 추출한 사용자 ID
     * @param request 온보딩 정보
     * @return 온보딩 완료 응답
     */
    @Transactional
    public OnboardingResponse completeOnboarding(Long userId, OnboardingRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));

        // 2. 이미 온보딩을 완료했는지 확인
        if (user.getHasCompletedOnboarding()) {
            throw new IllegalStateException("이미 온보딩을 완료한 사용자입니다.");
        }

        // 3. UserPreference 생성 또는 업데이트
        UserPreference userPreference = userPreferenceRepository.findByUser_UserId(userId)
                .orElse(UserPreference.builder().build());

        userPreference = UserPreference.builder()
                .preferenceId(userPreference.getPreferenceId()) // 기존 ID 유지 (업데이트의 경우)
                .user(user)
                .hasRunningHistory(request.getHasRunningHistory())
                .fitnessLevel(request.getFitnessLevel())
                .targetDistanceKm(request.getTargetDistanceKm())
                .restingHeartRate(request.getRestingHeartRate())
                .hasSmartWatch(request.getHasSmartWatch())
                .build();

        // 4. 사용자 온보딩 완료 처리
        user.completeOnboarding();
        user.setUserPreference(userPreference);

        // 5. 저장
        userPreferenceRepository.save(userPreference);
        userRepository.save(user);

        // 6. 응답 생성
        return OnboardingResponse.builder()
                .userId(user.getUserId())
                .onboardingCompletedAt(user.getUpdatedAt())
                .profile(OnboardingResponse.ProfileDto.builder()
                        .hasRunningHistory(userPreference.getHasRunningHistory())
                        .fitnessLevel(userPreference.getFitnessLevel())
                        .targetDistanceKm(userPreference.getTargetDistanceKm())
                        .restingHeartRate(userPreference.getRestingHeartRate())
                        .hasSmartWatch(userPreference.getHasSmartWatch())
                        .build())
                .build();
    }

    /**
     * 사용자 온보딩 상태 조회
     * @param userId 사용자 ID
     * @return 온보딩 완료 여부
     */
    public boolean isOnboardingCompleted(Long userId) {
        return userRepository.findById(userId)
                .map(User::getHasCompletedOnboarding)
                .orElse(false);
    }
}

