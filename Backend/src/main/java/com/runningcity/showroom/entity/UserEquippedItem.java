package com.runningcity.showroom.entity;

import com.runningcity.boutique.enums.ItemCategory;
import com.runningcity.boutique.enums.Style;
import com.runningcity.boutique.enums.SubCategory;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "subcategory", nullable = false, length = 50)
    private SubCategory subcategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "style", length = 50)
    private Style style;

    @CreationTimestamp
    @Column(name = "equipped_at", nullable = false, updatable = false)
    private ZonedDateTime equippedAt;

    // ============================================
    // 비즈니스 메서드
    // ============================================

    /**
     * 아이템 교체 (같은 슬롯에 새 아이템 장착)
     */
    public void changeItem(Long newItemId) {
        if (newItemId == null || newItemId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 아이템 ID입니다.");
        }
        this.itemId = newItemId;
    }

    /**
     * 같은 슬롯인지 확인
     */
    public boolean isSameSlot(ItemCategory category, SubCategory subcategory) {
        return this.category == category &&
                this.subcategory == subcategory;
    }

    /**
     * 특정 아이템이 장착되어 있는지 확인
     */
    public boolean isEquipped(Long itemId) {
        return this.itemId.equals(itemId);
    }


    // 정적 팩터리 메서드
    public static UserEquippedItem create(
            Long userId,
            Long itemId,
            ItemCategory category,
            SubCategory subcategory,
            Style style,
            ZonedDateTime equippedAt
    ) {
        UserEquippedItem ei = new UserEquippedItem();
        ei.userId = userId;
        ei.itemId = itemId;
        ei.category = category;
        ei.subcategory = subcategory;
        ei.style = style;
        ei.equippedAt = equippedAt;
        return ei;
    }

}