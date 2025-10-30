package com.runningcity.mission.service;

import com.runningcity.global.common.CommonResponseCode;
import com.runningcity.mission.dto.DailyMissionProgressRequest;
import com.runningcity.mission.dto.DailyMissionResponse;
import com.runningcity.mission.entity.DailyMission;
import com.runningcity.mission.repository.DailyMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@Transactional
public class DailyMissionService {

    private final DailyMissionRepository dailyMissionRepository;

    public DailyMissionService(DailyMissionRepository dailyMissionRepository) {
        this.dailyMissionRepository = dailyMissionRepository;
    }

    private ZonedDateTime nowSeoul() {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    private ZonedDateTime todayStartSeoul() {
        return nowSeoul().toLocalDate().atStartOfDay(ZoneId.of("Asia/Seoul"));
    }

    public DailyMissionResponse getTodayMission(Long userId) {
        ZonedDateTime today = todayStartSeoul();
        DailyMission mission = dailyMissionRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> createDefaultMission(userId, today));
        return toResponse(mission);
    }

    public DailyMissionResponse addProgress(Long userId, DailyMissionProgressRequest request) {
        ZonedDateTime today = todayStartSeoul();
        DailyMission mission = dailyMissionRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> createDefaultMission(userId, today));

        double add = request.getAdditionalKm();
        if (add < 0) {
            throw new IllegalArgumentException(CommonResponseCode.BAD_REQUEST.getMessage());
        }

        double newKm = Math.max(0, mission.getCurrentKm() + add);
        mission.setCurrentKm(newKm);

        if (newKm >= mission.getTargetKm()) {
            mission.setCompleted(true);
        }

        mission.setUpdatedAt(nowSeoul());
        DailyMission saved = dailyMissionRepository.save(mission);
        return toResponse(saved);
    }

    public DailyMissionResponse claim(Long userId, Long missionId) {
        DailyMission mission = dailyMissionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException(CommonResponseCode.DAILY_MISSION_NOT_FOUND.getMessage()));

        if (!mission.getUserId().equals(userId)) {
            throw new IllegalArgumentException(CommonResponseCode.FORBIDDEN.getMessage());
        }

        if (!mission.isCompleted()) {
            throw new IllegalArgumentException(CommonResponseCode.DAILY_MISSION_NOT_COMPLETED.getMessage());
        }

        if (mission.isClaimed()) {
            throw new IllegalArgumentException(CommonResponseCode.DAILY_MISSION_ALREADY_CLAIMED.getMessage());
        }

        mission.setClaimed(true);
        mission.setUpdatedAt(nowSeoul());
        DailyMission saved = dailyMissionRepository.save(mission);
        return toResponse(saved);
    }

    private DailyMission createDefaultMission(Long userId, ZonedDateTime date) {
        DailyMission mission = new DailyMission();
        mission.setUserId(userId);
        mission.setDate(date);
        mission.setTargetKm(5.0);
        mission.setCurrentKm(0.0);
        mission.setCompleted(false);
        mission.setClaimed(false);
        mission.setRewardCoins(50);
        mission.setUpdatedAt(nowSeoul());
        return dailyMissionRepository.save(mission);
    }

    private DailyMissionResponse toResponse(DailyMission mission) {
        int progress = 0;
        if (mission.getTargetKm() > 0) {
            progress = (int) Math.min(
                    100,
                    Math.round((mission.getCurrentKm() / mission.getTargetKm()) * 100.0)
            );
        }

        return DailyMissionResponse.builder()
                .missionId(mission.getMissionId())
                .userId(mission.getUserId())
                .missionDate(mission.getDate())
                .targetKm(mission.getTargetKm())
                .currentKm(mission.getCurrentKm())
                .progressPercent(progress)
                .completed(mission.isCompleted())
                .claimed(mission.isClaimed())
                .rewardCoins(mission.getRewardCoins())
                .updatedAt(mission.getUpdatedAt())
                .build();
    }
}
