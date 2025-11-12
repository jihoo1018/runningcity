package com.runningcity.showroom.entity;

import com.runningcity.boutique.exception.BoutiqueResponseCode;
import com.runningcity.global.exception.BaseException;
import com.runningcity.global.response.CommonResponseCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 유저 인벤토리 테이블
 * 유저가 소유한 아이템 관리 (중복 허용, quantity로 관리)
 */

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_inventory",
        uniqueConstraints = {@UniqueConstraint(  name = "uk_user_item", columnNames = {"user_id", "item_id"}) })
public class UserInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "first_obtained_at", insertable = false, updatable = false)
    private Instant firstObtainedAt;

    @Column(name = "last_obtained_at", insertable = false, updatable = false)
    private Instant lastObtainedAt;

    /**
     * 아이템 수량 증가
     * 가챠에서 중복 획득 시 사용
     * entity의 상태를 변경하는 주체는 자신이어야 해서 여기에 위치함이 맞음
     */
    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }
    /**
     * 새로운 인벤토리 아이템 생성
     * @param userId 유저 ID
     * @param itemId 아이템 ID
     * @return 생성된 인벤토리
     */
    public static UserInventory create(Long userId, Long itemId) {
        validateUserId(userId);
        validateItemId(itemId);

        UserInventory inventory = new UserInventory();
        inventory.userId = userId;
        inventory.itemId = itemId;
        return inventory;
    }
    // ============================================
    // 검증 메서드
    // ============================================

    private static void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BaseException(CommonResponseCode.USER_NOT_FOUND);
        }
    }

    private static void validateItemId(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new BaseException(BoutiqueResponseCode.ITEM_NOT_FOUND);
        }
    }



}