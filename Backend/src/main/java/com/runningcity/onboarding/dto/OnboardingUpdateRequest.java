package com.runningcity.onboarding.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingUpdateRequest {

    @NotNull(message = "목표 거리는 필수입니다.")
    @DecimalMin(value = "1.0", message = "목표 거리는 최소 1.0km 이상이어야 합니다.")
    @DecimalMax(value = "40.0", message = "목표 거리는 최대 40.0km 이하여야 합니다.")
    private Float targetDistanceKm;
}

