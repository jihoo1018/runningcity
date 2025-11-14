package com.runningcity.showroom.dto;

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
//    private Instant firstObtainedAt;
//    private Instant lastObtainedAt;

    // Entity → DTO 변환 (JPA에서 가져온 Entity를 Response로 매핑)
    public static UserInventoryResponse fromEntity(UserInventory entity) {
        return UserInventoryResponse.builder()
                .inventoryId(entity.getInventoryId())
//                .userId(entity.getUserId())
                .itemId(entity.getItemId())
                .quantity(entity.getQuantity())
//                .firstObtainedAt(entity.getFirstObtainedAt())
//                .lastObtainedAt(entity.getLastObtainedAt())
                .build();
    }
}
