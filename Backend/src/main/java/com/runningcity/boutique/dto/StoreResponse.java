package com.runningcity.boutique.dto;

import com.runningcity.boutique.entity.Boutique;
import com.runningcity.boutique.enums.ItemCategory;
import com.runningcity.boutique.enums.RarityType;
import com.runningcity.boutique.enums.Style;
import com.runningcity.boutique.enums.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 부티크 스토어 아이템 응답 DTO
 * obtainMethod = "store"인 구매 가능 아이템 목록
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreResponse {

    private Long itemId;              // 아이템 고유 ID
    private ItemCategory category;    // 카테고리 (BODY, HAIR, etc.)
    private SubCategory subcategory;
    private Style style;
    private String name;              // 아이템 이름
    private String assetKey;          // 에셋 키
    private String basePath;              // 이미지 경로/URL
    private RarityType rarity;        // 희귀도
    private Integer priceCr;          // 크레딧 가격
    private Boolean isPurchased;      // 구매 여부 (userInventory 체크)
    /**
     * Entity → DTO 변환 (구매 여부 포함)
     */
    public static StoreResponse from(Boutique boutique, boolean isPurchased) {
        return StoreResponse.builder()
                .itemId(boutique.getItemId())
                .category(boutique.getCategory())
                .subcategory(boutique.getSubcategory())
                .style(boutique.getStyle())
                .name(boutique.getName())
                .assetKey(boutique.getAssetKey())
                .basePath(boutique.getBasePath())
                .rarity(boutique.getRarity())
                .priceCr(boutique.getPriceCr())
                .isPurchased(isPurchased)
                .build();
    }
}