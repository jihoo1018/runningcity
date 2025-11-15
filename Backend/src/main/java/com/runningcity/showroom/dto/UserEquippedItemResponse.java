package com.runningcity.showroom.dto;

import com.runningcity.boutique.enums.ItemCategory;
import com.runningcity.boutique.enums.SubCategory;
import com.runningcity.showroom.entity.UserEquippedItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEquippedItemResponse {
    private Long equippedId;
    //    private Long userId;
    private Long itemId;
    private ItemCategory category;
    private SubCategory subcategory;
//    private ZonedDateTime equippedAt;

    // Entity → DTO 변환 (JPA에서 가져온 Entity를 Response로 매핑)
    public static UserEquippedItemResponse fromEntity(UserEquippedItem entity) {
        return UserEquippedItemResponse.builder()
                .equippedId(entity.getEquippedId())
//                .userId(entity.getUserId())
                .itemId(entity.getItemId())
                .category(entity.getCategory())
                .subcategory(entity.getSubcategory())
//                .equippedAt(entity.getEquippedAt())
                .build();
    }
}
