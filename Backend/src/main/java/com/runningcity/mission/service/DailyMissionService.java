package com.runningcity.mission.service;

import com.runningcity.mission.entity.DailyMission;
import com.runningcity.mission.dto.DailyMissionModal;
import com.runningcity.mission.repository.DailyMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@Transactional
public class DailyMissionService {

    private final DailyMissionRepository repo;

    public DailyMissionService(DailyMissionRepository repo) {
        this.repo = repo;
    }

    private LocalDate todaySeoul() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"));
    }

    /** 모달: 오늘 미션이 없으면 기본값으로 생성해서 반환 */
    public DailyMissionModal getTodayModal() {
        var today = todaySeoul();
        DailyMission m = repo.findByDate(today).orElseGet(() -> {
            DailyMission n = new DailyMission();
            n.setDate(today);
            n.setTargetKm(5.0);     // ✅ 기본 목표
            n.setCurrentKm(0.0);
            n.setCompleted(false);
            n.setClaimed(false);
            n.setRewardCoins(50);   // ✅ 기본 보상
            return repo.save(n);
        });
        return DailyMissionModal.from(m);
    }

    /** 진행 km 추가 */
    public void addKm(Long id, double addKm) {
        DailyMission m = repo.findById(id).orElseThrow();
        m.setCurrentKm(Math.max(0, m.getCurrentKm() + addKm));
        if (m.getCurrentKm() >= m.getTargetKm()) {
            m.setCompleted(true);
        }
        repo.save(m);
    }

    /** 보상 수령 */
    public int claim(Long id) {
        DailyMission m = repo.findById(id).orElseThrow();
        if (!m.isCompleted()) throw new IllegalStateException("미션 미완료");
        if (m.isClaimed()) return 0;
        m.setClaimed(true);
        repo.save(m);
        return m.getRewardCoins();
    }

    /** 초기화(테스트용) */
    public void resetToday() {
        var today = todaySeoul();
        repo.findByDate(today).ifPresent(m -> {
            m.setCurrentKm(0);
            m.setCompleted(false);
            m.setClaimed(false);
            repo.save(m);
        });
    }

}
