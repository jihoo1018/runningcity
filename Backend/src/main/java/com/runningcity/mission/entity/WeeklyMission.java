// src/main/java/com/runningcity/mission/entity/WeeklyMission.java
package com.runningcity.mission.entity;

import jakarta.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "weekly_mission",
       uniqueConstraints = @UniqueConstraint(name = "uk_weekly_mission_user_week",
                                             columnNames = {"user_id", "week_start"}))
public class WeeklyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_mission_id")
    private Long weeklyMissionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "week_start", nullable = false)
    private ZonedDateTime weekStart;

    @Column(name = "target_days", nullable = false)
    private int targetDays = 5;

    @Column(name = "completed_days", nullable = false)
    private int completedDays = 0;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "claimed", nullable = false)
    private boolean claimed = false;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    void onCreate() {
        var now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        createdAt = (createdAt == null) ? now : createdAt;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    // === getters/setters ===
    public Long getWeeklyMissionId() { return weeklyMissionId; }
    public void setWeeklyMissionId(Long id) { this.weeklyMissionId = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public ZonedDateTime getWeekStart() { return weekStart; }
    public void setWeekStart(ZonedDateTime weekStart) { this.weekStart = weekStart; }

    public int getTargetDays() { return targetDays; }
    public void setTargetDays(int targetDays) { this.targetDays = targetDays; }

    public int getCompletedDays() { return completedDays; }
    public void setCompletedDays(int completedDays) { this.completedDays = completedDays; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
