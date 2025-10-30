package com.runningcity.mission.entity;

import jakarta.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "daily_mission",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_mission_user_date",
                columnNames = {"user_id", "date"}
        )
)
public class DailyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long missionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // DB가 timestamptz 또는 timestamp 여야 함
    @Column(name = "date", nullable = false)
    private ZonedDateTime date;

    @Column(name = "target_km", nullable = false)
    private double targetKm;

    @Column(name = "current_km", nullable = false)
    private double currentKm;

    
    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    
    @Column(name = "claimed", nullable = false)
    private boolean claimed = false;

    @Column(name = "reward_coins", nullable = false)
    private int rewardCoins = 0;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public DailyMission() {
    }

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public double getTargetKm() {
        return targetKm;
    }

    public void setTargetKm(double targetKm) {
        this.targetKm = targetKm;
    }

    public double getCurrentKm() {
        return currentKm;
    }

    public void setCurrentKm(double currentKm) {
        this.currentKm = currentKm;
    }

    
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public void setRewardCoins(int rewardCoins) {
        this.rewardCoins = rewardCoins;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
