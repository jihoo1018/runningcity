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
@AllArgsConstructor
@Builder
public class UserEquippedItemRequest {
//    private Long equippedId;
    private Long itemId;
    private ItemCategory category;
    private SubCategory subcategory;
    private Style style;
    private String basePath;

    // Entity → DTO 변환 (JPA에서 가져온 Entity를 Response로 매핑)
    public static UserEquippedItemRequest fromEntity(UserEquippedItem entity) {
        return UserEquippedItemRequest.builder()
//                .equippedId(entity.getEquippedId())
                .itemId(entity.getItemId())
                .category(entity.getCategory())
                .subcategory(entity.getSubcategory())
                .style(entity.getStyle())
//                .basePath(entity.getBasePath())
                .build();
    }
}
