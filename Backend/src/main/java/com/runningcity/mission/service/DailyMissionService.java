package com.runningcity.mission.service;

import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.mission.dto.DailyMissionResponse;
import com.runningcity.mission.entity.DailyMission;
import com.runningcity.mission.repository.DailyMissionRepository;
import com.runningcity.mission.repository.RunSessionReadRepository;
import com.runningcity.onboarding.entity.UserPreference;
import com.runningcity.onboarding.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
@Transactional
public class DailyMissionService {

    private static final double DEFAULT_TARGET_KM = 5.0;

    private final DailyMissionRepository dailyMissionRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final RunSessionReadRepository runSessionReadRepository;

    public DailyMissionService(
            DailyMissionRepository dailyMissionRepository,
            UserPreferenceRepository userPreferenceRepository,
            RunSessionReadRepository runSessionReadRepository
    ) {
        this.dailyMissionRepository = dailyMissionRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.runSessionReadRepository = runSessionReadRepository;
    }

    private ZonedDateTime nowSeoul() {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    private ZonedDateTime todayStartSeoul() {
        return nowSeoul().toLocalDate().atStartOfDay(ZoneId.of("Asia/Seoul"));
    }

    private ZonedDateTime todayEndSeoul() {
        return todayStartSeoul().plusDays(1);
    }

    /**
     * 오늘 미션 조회
     */
    public DailyMissionResponse getTodayMission(Long userId) {
        ZonedDateTime todayStart = todayStartSeoul();
        ZonedDateTime todayEnd = todayEndSeoul();

        double targetKm = loadTargetKm(userId);
        double todayRunKm = loadTodayRunKm(userId, todayStart, todayEnd);

        DailyMission mission = dailyMissionRepository.findByUserIdAndDate(userId, todayStart)
                .orElseGet(() -> createDefaultMission(userId, todayStart, targetKm));

        mission.setTargetKm(targetKm);
        mission.setCurrentKm(todayRunKm);
        mission.setCompleted(todayRunKm >= targetKm);
        mission.setUpdatedAt(nowSeoul());

        DailyMission saved = dailyMissionRepository.save(mission);
        return toResponse(saved);
    }

    /**
     * 오늘 보상 수령
     */
    public DailyMissionResponse claimToday(Long userId) {
        ZonedDateTime todayStart = todayStartSeoul();
        ZonedDateTime todayEnd = todayEndSeoul();

        DailyMission mission = dailyMissionRepository.findByUserIdAndDate(userId, todayStart)
                .orElseThrow(() -> new IllegalArgumentException(CommonResponseCode.DAILY_MISSION_NOT_FOUND.getMessage()));

        double targetKm = loadTargetKm(userId);
        double todayRunKm = loadTodayRunKm(userId, todayStart, todayEnd);

        mission.setTargetKm(targetKm);
        mission.setCurrentKm(todayRunKm);
        mission.setCompleted(todayRunKm >= targetKm);

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

    /**
     * 테스트용 초기화
     */
    public void resetToday(Long userId) {
        ZonedDateTime todayStart = todayStartSeoul();
        double targetKm = loadTargetKm(userId);

        DailyMission mission = dailyMissionRepository.findByUserIdAndDate(userId, todayStart)
                .orElseGet(() -> createDefaultMission(userId, todayStart, targetKm));

        mission.setCurrentKm(0.0);
        mission.setCompleted(false);
        mission.setClaimed(false);
        mission.setTargetKm(targetKm);
        mission.setUpdatedAt(nowSeoul());

        dailyMissionRepository.save(mission);
    }

    private DailyMission createDefaultMission(Long userId, ZonedDateTime date, double targetKm) {
        DailyMission mission = new DailyMission();
        mission.setUserId(userId);
        mission.setDate(date);
        mission.setTargetKm(targetKm);
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

    /**
     * 온보딩에서 목표 km 읽기
     */
    private double loadTargetKm(Long userId) {
        return userPreferenceRepository.findByUser_UserId(userId)
                .map(UserPreference::getTargetDistanceKm)
                .filter(km -> km != null && km > 0)
                .orElse((float)DEFAULT_TARGET_KM);
    }

    /**
     * run_session 테이블에서 오늘 뛴 거리 합계 읽기
     */
    private double loadTodayRunKm(Long userId, ZonedDateTime start, ZonedDateTime end) {
        return runSessionReadRepository.sumTodayDistance(
                userId,
                start.toOffsetDateTime(),
                end.toOffsetDateTime()
        );
    }
}
