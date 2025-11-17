// src/main/java/com/runningcity/mission/service/WeeklyMissionService.java
package com.runningcity.mission.service;

import com.runningcity.mission.dto.WeeklyMissionResponse;
import com.runningcity.mission.entity.WeeklyMission;
import com.runningcity.mission.repository.DailyMissionRepository;
import com.runningcity.mission.repository.WeeklyMissionRepository;
import com.runningcity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
@Transactional
@RequiredArgsConstructor
public class WeeklyMissionService {

    private static final int DEFAULT_TARGET_DAYS = 5; // 기본 목표 일수

    // ✅ 주간 보상 규칙 상수
    private static final long BASE_EXP_PER_DAY = 150;   // (완료 1회당) 150 EXP
    private static final long GOAL_BONUS_PER_DAY = 100; // (설정 목표 달성 시) 목표일수 × 100 EXP
    private static final long SEVEN_DAYS_BONUS = 1000;  // (7일 모두 달성 시) +1000 EXP

    private final WeeklyMissionRepository weeklyMissionRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final UserRepository userRepository;

    /* ====== 시간 헬퍼 (KST) ====== */
    private ZonedDateTime nowSeoul() {
        return ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }
    private ZonedDateTime weekStartSeoul() {
        var now = nowSeoul();
        return now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                  .toLocalDate()
                  .atStartOfDay(ZoneId.of("Asia/Seoul"));
    }
    private ZonedDateTime nextWeekStartSeoul() {
        return weekStartSeoul().plusWeeks(1);
    }

    /* ====== 조회 ====== */
    public WeeklyMissionResponse getThisWeek(Long userId) {
        var weekStart = weekStartSeoul();
        var weekEnd   = nextWeekStartSeoul();

        var mission = weeklyMissionRepository.findByUserIdAndWeekStart(userId, weekStart)
                .orElseGet(() -> {
                    var m = new WeeklyMission();
                    m.setUserId(userId);
                    m.setWeekStart(weekStart);
                    m.setTargetDays(DEFAULT_TARGET_DAYS);
                    return weeklyMissionRepository.save(m);
                });

        int completedDays = dailyMissionRepository
                .countByUserIdAndDateGreaterThanEqualAndDateLessThanAndCompletedTrue(
                        userId, weekStart, weekEnd);

        mission.setCompletedDays(completedDays);
        mission.setCompleted(completedDays >= mission.getTargetDays());

        var saved = weeklyMissionRepository.save(mission);
        return toResponse(saved, weekEnd);
    }

    /* ====== 보상 수령 ====== */
    public WeeklyMissionResponse claimThisWeek(Long userId) {
        var weekStart = weekStartSeoul();
        var weekEnd   = nextWeekStartSeoul();

        var mission = weeklyMissionRepository.findByUserIdAndWeekStart(userId, weekStart)
                .orElseThrow(() -> new IllegalArgumentException("주간 미션을 찾을 수 없습니다."));

        int completedDays = dailyMissionRepository
                .countByUserIdAndDateGreaterThanEqualAndDateLessThanAndCompletedTrue(
                        userId, weekStart, weekEnd);

        mission.setCompletedDays(completedDays);
        mission.setCompleted(completedDays >= mission.getTargetDays());

        if (!mission.isCompleted()) {
            throw new IllegalArgumentException("아직 주간 목표를 달성하지 못했습니다.");
        }
        if (mission.isClaimed()) {
            throw new IllegalArgumentException("이미 주간 보상을 수령했습니다.");
        }

        // 주간 보상 EXP 계산
        long totalExp = computeWeeklyRewardExp(completedDays, mission.getTargetDays());

        // 크레딧 전환(5%) — 일퀘와 동일 규칙: 총 EXP ÷ 20 (내림)
        long credit = totalExp / 20;

        // users 테이블 적립 (EXP, CREDIT)
        userRepository.addExpAndCredit(userId, totalExp, credit);

        mission.setClaimed(true);
        var saved = weeklyMissionRepository.save(mission);
        return toResponse(saved, weekEnd);
    }

    /** 주간 보상 EXP 계산식
     *  - 기본: 완료횟수 × 150
     *  - 목표 달성 보너스: (완료횟수 >= 목표일수) 이면 목표일수 × 100
     *  - 7일 보너스: 완료횟수 == 7 이면 +1000
     */
    private long computeWeeklyRewardExp(int completedDays, int targetDays) {
        if (completedDays < 0) completedDays = 0;
        if (targetDays < 0) targetDays = 0;

        long base = completedDays * BASE_EXP_PER_DAY;

        long goalBonus = (completedDays >= targetDays)
                ? (long) targetDays * GOAL_BONUS_PER_DAY
                : 0L;

        long sevenBonus = (completedDays >= 7) ? SEVEN_DAYS_BONUS : 0L;

        return base + goalBonus + sevenBonus;
    }

    /* ====== 변환 ====== */
    private WeeklyMissionResponse toResponse(WeeklyMission m, ZonedDateTime weekEnd) {
        int progress = (m.getTargetDays() > 0)
                ? Math.min(100, Math.round((m.getCompletedDays() * 100f) / m.getTargetDays()))
                : 0;

        return WeeklyMissionResponse.builder()
                .missionId(m.getWeeklyMissionId())
                .userId(m.getUserId())
                .weekStart(m.getWeekStart())
                .weekEnd(weekEnd)
                .targetDays(m.getTargetDays())
                .completedDays(m.getCompletedDays())
                .progressPercent(progress)
                .completed(m.isCompleted())
                .claimed(m.isClaimed())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
