package com.runningcity.showroom.entity;

import com.runningcity.boutique.enums.ItemCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(
    name = "user_equipped_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_category_subcategory",
        columnNames = {"user_id", "category", "subcategory"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserEquippedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipped_id")
    private Long equippedId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ItemCategory category;

    @Column(name = "subcategory", length = 30)
    private String subcategory;

    @CreationTimestamp
    @Column(name = "equipped_at", nullable = false, updatable = false)
    private ZonedDateTime equippedAt;

    // ============================================
    // 비즈니스 메서드
    // ============================================

    /**
     * 아이템 교체 (같은 슬롯에 새 아이템 장착)
     * @param newItemId 새로 장착할 아이템 ID
     */
    public void changeItem(Long newItemId) {
        if (newItemId == null || newItemId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 아이템 ID입니다.");
        }
        this.itemId = newItemId;
    }

    /**
     * 같은 슬롯인지 확인
     * @param category 카테고리
     * @param subcategory 서브카테고리
     * @return 같은 슬롯 여부
     */
    public boolean isSameSlot(ItemCategory category, String subcategory) {
        return this.category == category && 
               isSameSubcategory(subcategory);
    }

    /**
     * 서브카테고리 비교 (null 처리 포함)
     */
    private boolean isSameSubcategory(String other) {
        if (this.subcategory == null && other == null) {
            return true;
        }
        if (this.subcategory == null || other == null) {
            return false;
        }
        return this.subcategory.equals(other);
    }

    /**
     * 특정 아이템이 장착되어 있는지 확인
     * @param itemId 확인할 아이템 ID
     * @return 장착 여부
     */
    public boolean isEquipped(Long itemId) {
        return this.itemId.equals(itemId);
    }
}