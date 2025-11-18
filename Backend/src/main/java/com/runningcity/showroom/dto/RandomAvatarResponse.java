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
 * 용도: 친구/랜덤 유저들의 아바타 + 통계 + Privacy 정보 조회
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RandomAvatarResponse {

    // ===== 기본 정보 =====
    private Long userId;
    private String nickname;
    private Integer level;

    // ===== 통계 정보 =====
    private Double totalDist;
    private Double maxDist;
    private Double avgPace;
    private Double bestPace;
    private Long totalEntryCnt;

    // ===== Privacy 설정 =====
    private PrivacySettingResponse privacySetting;

    // ===== 장착 아이템 =====
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

        // ⭐ 새로 추가: 스프라이트 타입
        private String spriteType;  // "COMPLETE" or "COMPOSITE"
    }
}