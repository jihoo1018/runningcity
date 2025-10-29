package com.runningcity.onboarding.dto;

import com.runningcity.onboarding.entity.UserPreference.FitnessLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingRequest {

    @NotNull(message = "러닝 이력 여부는 필수입니다.")
    private Boolean hasRunningHistory;

    @NotNull(message = "운동 능력치는 필수입니다.")
    private FitnessLevel fitnessLevel;

    @NotNull(message = "목표 거리는 필수입니다.")
    @DecimalMin(value = "1.0", message = "목표 거리는 최소 1.0km 이상이어야 합니다.")
    @DecimalMax(value = "40.0", message = "목표 거리는 최대 40.0km 이하여야 합니다.")
    private Float targetDistanceKm;

    @Min(value = 40, message = "안정시 심박수는 최소 40bpm 이상이어야 합니다.")
    @Max(value = 120, message = "안정시 심박수는 최대 120bpm 이하여야 합니다.")
    private Integer restingHeartRate; // nullable

    @NotNull(message = "스마트워치 소유 여부는 필수입니다.")
    private Boolean hasSmartWatch;
}

