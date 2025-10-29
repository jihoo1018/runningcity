package com.runningcity.mission.dto;

import com.runningcity.mission.entity.DailyMission;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public record DailyMissionModal(
        Long id, String date, String serverTime,
        double targetKm, double currentKm, int progressPercent,
        boolean completed, boolean claimed, int rewardCoins
) {
    public static DailyMissionModal from(DailyMission m) {
        int pct = m.getTargetKm() <= 0 ? 0 :
                (int)Math.min(100, Math.round((m.getCurrentKm() / m.getTargetKm()) * 100.0));
        String today = m.getDate().toString();
        String server = ZonedDateTime.now(ZoneId.of("UTC")).toString();
        return new DailyMissionModal(
                m.getId(), today, server,
                m.getTargetKm(), m.getCurrentKm(), pct,
                m.isCompleted(), m.isClaimed(), m.getRewardCoins()
        );
    }
}
