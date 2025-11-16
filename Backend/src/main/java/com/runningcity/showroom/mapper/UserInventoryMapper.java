package com.runningcity.showroom.mapper;

import com.runningcity.showroom.dto.UserInventoryResponse;
import com.runningcity.showroom.entity.UserInventory;
import com.runningcity.boutique.entity.Boutique;

public class UserInventoryMapper {

    public static UserInventoryResponse toDto(UserInventory ui, Boutique b) {
        return UserInventoryResponse.builder()
                .inventoryId(ui.getInventoryId())
                .itemId(ui.getItemId())
                .quantity(ui.getQuantity())

                .category(b.getCategory())
                .subcategory(b.getSubcategory())
                .style(b.getStyle())
                .color(b.getColor())
                .name(b.getName())
                .assetKey(b.getAssetKey())
                .basePath(b.getBasePath())
                .rarity(b.getRarity())
                .priceCr(b.getPriceCr())
                .obtainMethod(b.getObtainMethod())
                .build();
    }
}
