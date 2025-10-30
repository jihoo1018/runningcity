package com.runningcity.mission.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 하루에 1개만 존재하는 일일 미션 엔티티
 * (현재는 user 연관이 없어서 전체 시스템에 하루 1개라는 의미)
 */
@Entity
@Table(
        name = "daily_mission",
        uniqueConstraints = @UniqueConstraint(name = "uk_daily_mission_date", columnNames = "date")
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 서울 기준의 "하루"를 유일하게 구분하기 위한 날짜
     */
    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "target_km", nullable = false)
    private double targetKm;

    @Column(name = "current_km", nullable = false)
    private double currentKm;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "claimed", nullable = false)
    private boolean claimed;

    @Column(name = "reward_coins", nullable = false)
    private int rewardCoins;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
