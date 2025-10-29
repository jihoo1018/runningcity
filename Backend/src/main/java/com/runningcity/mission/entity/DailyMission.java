package com.runningcity.mission.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "daily_mission",
    uniqueConstraints = @UniqueConstraint(name = "uk_daily_mission_date", columnNames = "date")
)
public class DailyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // YYYY-MM-DD (오늘 기준 1개만)
    @Column(nullable = false)
    private LocalDate date;

    // 목표 거리(km)
    @Column(nullable = false)
    private double targetKm;

    // 현재 진행 거리(km)
    @Column(nullable = false)
    private double currentKm = 0.0;

    // 목표 달성 여부
    @Column(nullable = false)
    private boolean completed = false;

    // 보상 수령 여부
    @Column(nullable = false)
    private boolean claimed = false;

    // 보상 코인
    @Column(nullable = false)
    private int rewardCoins = 0;

    // 갱신 시각
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }

    // ======= Getter / Setter =======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getTargetKm() { return targetKm; }
    public void setTargetKm(double targetKm) { this.targetKm = targetKm; }

    public double getCurrentKm() { return currentKm; }
    public void setCurrentKm(double currentKm) { this.currentKm = currentKm; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }

    public int getRewardCoins() { return rewardCoins; }
    public void setRewardCoins(int rewardCoins) { this.rewardCoins = rewardCoins; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
