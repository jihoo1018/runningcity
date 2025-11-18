package com.runningcity.showroom.dto;

import com.runningcity.boutique.enums.ItemCategory;
import com.runningcity.boutique.enums.Style;
import com.runningcity.boutique.enums.SubCategory;
import com.runningcity.showroom.entity.UserEquippedItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class UserEquippedItemResponse {
    private Long equippedId;
    private Long itemId;
    private ItemCategory category;
    private SubCategory subcategory;
    private Style style;
    private String basePath;
    private String spriteType;

    // ⭐ JPQL의 new 생성자용 - 7개 파라미터
    public UserEquippedItemResponse(
            Long equippedId,
            Long itemId,
            ItemCategory category,
            SubCategory subcategory,
            Style style,
            String basePath,
            String spriteType
    ) {
        this.equippedId = equippedId;
        this.itemId = itemId;
        this.category = category;
        this.subcategory = subcategory;
        this.style = style;
        this.basePath = basePath;
        this.spriteType = spriteType;
    }

    // Entity → DTO 변환 (거의 사용 안 함)
    public static UserEquippedItemResponse fromEntity(UserEquippedItem entity) {
        return UserEquippedItemResponse.builder()
                .equippedId(entity.getEquippedId())
                .itemId(entity.getItemId())
                .category(entity.getCategory())
                .subcategory(entity.getSubcategory())
                .style(entity.getStyle())
                .basePath(null)  // Entity에는 없음
                .spriteType("COMPOSITE")
                .build();
    }

    // ⭐ 완성 스프라이트용
    public static UserEquippedItemResponse forCompleteSprite(String spritePath) {
        return UserEquippedItemResponse.builder()
                .equippedId(null)
                .itemId(null)
                .category(null)
                .subcategory(null)
                .style(null)
                .basePath(spritePath)
                .spriteType("COMPLETE")
                .build();
    }
}