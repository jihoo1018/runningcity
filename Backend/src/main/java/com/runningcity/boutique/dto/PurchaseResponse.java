package com.runningcity.boutique.dto;

import com.runningcity.boutique.entity.Boutique;
import com.runningcity.showroom.entity.UserInventory;
import com.runningcity.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상점 구매 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseResponse {

    private Long itemId;              // 구매한 아이템 ID
    private String itemName;          // 아이템 이름
    private Integer paidCr;           // 지불한 크레딧
    private Long remainingCr;      // 남은 크레딧
    private Long showroomId;          // 생성된 쇼룸 ID (인벤토리 ID)

    /**
     * 구매 완료 후 엔티티들로부터 응답 DTO 생성
     */
    public static PurchaseResponse from(Boutique item, User user, UserInventory userInventory) {
        return PurchaseResponse.builder()
                .itemId(item.getItemId())
                .itemName(item.getName())
                .paidCr(item.getPriceCr())
                .remainingCr(user.getTotalCredit())
                .showroomId(userInventory.getInventoryId())
                .build();
    }
}