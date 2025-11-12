package com.runningcity.mission.service;

import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.mission.dto.DailyMissionResponse;
import com.runningcity.mission.entity.DailyMission;
import com.runningcity.mission.repository.DailyMissionRepository;
import com.runningcity.mission.repository.RunSessionReadRepository;
import com.runningcity.mission.repository.UserAccountWriteRepository;
import com.runningcity.onboarding.entity.UserPreference;
import com.runningcity.onboarding.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@Transactional
public class DailyMissionService {

    private static final double DEFAULT_TARGET_KM = 5.0;

    // 🔴 개발용: run_session에 기록 없으면 이 값으로 오늘 뛴 거리로 간주
    private static final boolean DEV_FAKE_TODAY_KM = true;
    private static final double DEV_FAKE_KM_VALUE = 5.0;

    private final DailyMissionRepository dailyMissionRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final RunSessionReadRepository runSessionReadRepository;
    private final UserAccountWriteRepository userAccountWriteRepository;

    public DailyMissionService(
            DailyMissionRepository dailyMissionRepository,
            UserPreferenceRepository userPreferenceRepository,
            RunSessionReadRepository runSessionReadRepository,
            UserAccountWriteRepository userAccountWriteRepository
    ) {
        this.dailyMissionRepository = dailyMissionRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.runSessionReadRepository = runSessionReadRepository;
        this.userAccountWriteRepository = userAccountWriteRepository;
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

        /*
         * ===== 보상 로직 =====
         * 기본 EXP = 거리 × 10
         * 연속 운동 보너스 = 일수 × 5 (최대 50)
         * 크레딧 전환 = 총 EXP ÷ 20 (결과는 내림)
         */
        long baseExp = Math.round(todayRunKm * 10);   // ex) 5km -> 50 EXP

        // TODO: 실제 연속운동일수 저장되면 여기서 조회
        int consecutiveDays = 0;
        long streakBonus = Math.min(consecutiveDays * 5L, 50L);

        long totalExpToAdd = baseExp + streakBonus;

        long creditToAdd = totalExpToAdd / 20; // 5% 전환

        // users 테이블에 경험치/크레딧 적립
        userAccountWriteRepository.addExpAndCredit(userId, totalExpToAdd, creditToAdd);

        // 미션 상태 마무리
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
                .orElse((float) DEFAULT_TARGET_KM);
    }

    /**
     * run_session 테이블에서 오늘 뛴 거리 합계 읽기
     * + 개발 중이면 0일 때 강제로 값 채우기
     */
    private double loadTodayRunKm(Long userId, ZonedDateTime start, ZonedDateTime end) {
        double real = runSessionReadRepository.sumTodayDistance(
                userId,
                start.toOffsetDateTime(),
                end.toOffsetDateTime()
        );

        // 개발모드 켜져있고, 오늘 기록이 전혀 없으면 가짜 값 사용
        if (DEV_FAKE_TODAY_KM && real <= 0.0001) {
            return DEV_FAKE_KM_VALUE;
        }

        return real;
    }
}
