package com.runningcity.showroom.dto;

import com.runningcity.boutique.enums.*;
import com.runningcity.showroom.entity.UserInventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInventoryResponse {
    private Long inventoryId;
    private Long itemId;
    private Integer quantity = 1;

    // Boutique 정보
    private ItemCategory category;
    private SubCategory subcategory;
    private Style style;
    private String color;
    private String name;
    private String assetKey;
    private String basePath;
    private RarityType rarity;
    private Integer priceCr;
    private ObtainMethod obtainMethod;

}
