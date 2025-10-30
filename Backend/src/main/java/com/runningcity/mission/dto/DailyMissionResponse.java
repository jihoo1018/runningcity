package com.runningcity.mission.dto;

import com.runningcity.mission.entity.DailyMission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 일일 미션 조회/수정 후 내려주는 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMissionResponse {

    private Long id;
    private String date;
    private String serverTime;
    private double targetKm;
    private double currentKm;
    private int progressPercent;
    private boolean completed;
    private boolean claimed;
    private int rewardCoins;

    public static DailyMissionResponse from(DailyMission mission) {
        int progress = 0;
        if (mission.getTargetKm() > 0) {
            progress = (int) Math.min(
                    100,
                    Math.round((mission.getCurrentKm() / mission.getTargetKm()) * 100.0)
            );
        }

        return DailyMissionResponse.builder()
                .id(mission.getId())
                .date(mission.getDate().toString())
                // 서버 기준 시간은 UTC로 내려줌
                .serverTime(ZonedDateTime.now(ZoneId.of("UTC")).toString())
                .targetKm(mission.getTargetKm())
                .currentKm(mission.getCurrentKm())
                .progressPercent(progress)
                .completed(mission.isCompleted())
                .claimed(mission.isClaimed())
                .rewardCoins(mission.getRewardCoins())
                .build();
    }
}
