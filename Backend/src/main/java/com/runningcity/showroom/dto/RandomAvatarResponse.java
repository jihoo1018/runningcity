package com.runningcity.showroom.dto;

import com.runningcity.boutique.enums.ItemCategory;
import com.runningcity.boutique.enums.Style;
import com.runningcity.boutique.enums.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 랜덤 유저 아바타 쇼룸 응답 DTO
 * 용도: 친구가 아닌 랜덤 유저들의 아바타 정보 조회
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RandomAvatarResponse {

    private Long userId;
    private String nickname;
    private Integer level;
    private List<EquippedItemDto> equippedItems;

    /**
     * 🎨 장착 아이템 상세 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EquippedItemDto {

        private Long itemId;
        private ItemCategory category;
        private SubCategory subcategory;
        private Style style;
        private String basePath;
    }
}