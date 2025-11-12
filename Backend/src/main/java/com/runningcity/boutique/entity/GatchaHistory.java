package com.runningcity.boutique.entity;

import com.runningcity.boutique.enums.RarityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 가챠 뽑기 이력 테이블
 * 유저가 실제로 뽑기를 수행한 내역을 기록
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "gacha_history")
public class GatchaHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RarityType rarity;

    @Column(name = "draw_type", length = 20)
    private String drawType;  // single, multi_10

    @Column(name = "draw_time", insertable = false, updatable = false)
    private Instant drawTime;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(nullable = false)
    private Boolean obtained;

    @Column(columnDefinition = "TEXT")
    private String notes;

}