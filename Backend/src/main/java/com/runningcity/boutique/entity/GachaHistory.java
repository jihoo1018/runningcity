package com.runningcity.boutique.entity;

import com.runningcity.boutique.enums.DrawType;
import com.runningcity.boutique.enums.RarityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * 가챠 뽑기 이력 테이블
 * 유저가 실제로 뽑기를 수행한 내역을 기록
 */
@Entity
@Table(name = "gacha_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GachaHistory {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DrawType drawType;  // "single", "multi"

    @CreationTimestamp
    @Column(name = "draw_time", nullable = false, updatable = false)
    private Instant drawTime;

    @Column(name = "session_id")
    private UUID sessionId;

    // ============================================
    // 정적 팩토리 메서드
    // ============================================

    /**
     * 가챠 이력 생성
     */
    public static GachaHistory create(
            Long userId,
            Long itemId,
            RarityType rarity,
            DrawType drawType,
            UUID sessionId
    ) {
        GachaHistory history = new GachaHistory();
        history.userId = userId;
        history.itemId = itemId;
        history.rarity = rarity;
        history.drawType = drawType;
        history.sessionId = sessionId;
        return history;
    }

}