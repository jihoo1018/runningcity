package com.runningcity.mission.service;

import com.runningcity.mission.dto.DailyMissionProgressRequest;
import com.runningcity.mission.dto.DailyMissionResponse;
import com.runningcity.mission.entity.DailyMission;
import com.runningcity.mission.repository.DailyMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 일일 미션 비즈니스 로직
 * 온보딩 서비스 구조에 맞춰 작성
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DailyMissionService {

    private final DailyMissionRepository dailyMissionRepository;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private LocalDate todaySeoul() {
        return LocalDate.now(SEOUL);
    }

    /**
     * 오늘 미션 조회 (없으면 생성)
     */
    public DailyMissionResponse getTodayMission(Long userId) {
        LocalDate today = todaySeoul();

        DailyMission mission = dailyMissionRepository.findByDate(today)
                .orElseGet(() -> createNewMissionFor(today));

        return DailyMissionResponse.from(mission);
    }

    /**
     * 오늘 미션 진행도 갱신
     */
    public DailyMissionResponse updateTodayProgress(Long userId, DailyMissionProgressRequest request) {
        LocalDate today = todaySeoul();

        DailyMission mission = dailyMissionRepository.findByDate(today)
                .orElseGet(() -> createNewMissionFor(today));

        double newKm = mission.getCurrentKm() + request.getAdditionalKm();
        mission.setCurrentKm(newKm);

        // 완료 여부 판정
        if (mission.getTargetKm() > 0 && newKm >= mission.getTargetKm()) {
            mission.setCompleted(true);
        }

        mission.setUpdatedAt(Instant.now());
        dailyMissionRepository.save(mission);

        return DailyMissionResponse.from(mission);
    }

    /**
     * 보상 수령
     */
    public DailyMissionResponse claim(Long userId, Long missionId) {
        DailyMission mission = dailyMissionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("미션을 찾을 수 없습니다. id=" + missionId));

        if (mission.isClaimed()) {
            throw new IllegalStateException("이미 보상을 수령한 미션입니다.");
        }

        if (!mission.isCompleted()) {
            throw new IllegalStateException("미션이 완료되지 않아 보상을 받을 수 없습니다.");
        }

        mission.setClaimed(true);
        mission.setUpdatedAt(Instant.now());
        dailyMissionRepository.save(mission);

        return DailyMissionResponse.from(mission);
    }

    /**
     * (개발용) 오늘 미션 초기화
     */
    public void resetToday(Long userId) {
        LocalDate today = todaySeoul();
        dailyMissionRepository.findByDate(today).ifPresent(m -> {
            m.setCurrentKm(0.0);
            m.setCompleted(false);
            m.setClaimed(false);
            m.setUpdatedAt(Instant.now());
            dailyMissionRepository.save(m);
        });
    }

    /**
     * 오늘 날짜로 새로운 미션 생성
     */
    private DailyMission createNewMissionFor(LocalDate date) {
        DailyMission mission = DailyMission.builder()
                .date(date)
                .targetKm(3.0)        // 기본 목표값
                .currentKm(0.0)
                .completed(false)
                .claimed(false)
                .rewardCoins(50)
                .updatedAt(Instant.now())
                .build();
        return dailyMissionRepository.save(mission);
    }
}
