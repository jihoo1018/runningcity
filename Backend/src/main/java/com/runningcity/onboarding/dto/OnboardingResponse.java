package com.runningcity.onboarding.dto;

import com.runningcity.onboarding.entity.UserPreference.FitnessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingResponse {

    private Long userId;
    private ZonedDateTime onboardingCompletedAt;
    private ProfileDto profile;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileDto {
        private Boolean hasRunningHistory;
        private FitnessLevel fitnessLevel;
        private Float targetDistanceKm;
        private Integer restingHeartRate;
        private Boolean hasSmartWatch;
    }
}

