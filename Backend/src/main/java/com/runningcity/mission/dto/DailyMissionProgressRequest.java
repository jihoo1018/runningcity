package com.runningcity.mission.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오늘 미션 진행도를 추가로 올릴 때 사용하는 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyMissionProgressRequest {

    @NotNull(message = "추가 주행 거리는 필수입니다.")
    @DecimalMin(value = "0.1", message = "추가 주행 거리는 최소 0.1km 이상이어야 합니다.")
    private Double additionalKm;
}
