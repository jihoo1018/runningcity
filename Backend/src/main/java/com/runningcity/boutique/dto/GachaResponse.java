package com.runningcity.boutique.dto;

import com.runningcity.boutique.enums.DrawType;
import com.runningcity.boutique.enums.ItemCategory;
import com.runningcity.boutique.enums.RarityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GachaResponse {
    private UUID sessionId;           // 세션 ID (10연차 묶음 식별용)
    private DrawType drawType;        // single or multi
    private Integer totalDraws;       // 총 뽑은 개수 (1 or 10)
    private Long spentCredit;         // 소비한 크레딧 (50 or 450)
    private Long remainingCredit;     // 남은 크레딧
    private List<GachaItem> items;    // 뽑은 아이템 목록

    /**
     * 뽑은 아이템 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GachaItem {
        private Long itemId;          // 아이템 ID
        private String itemName;      // 아이템 이름
        private ItemCategory category; // 카테고리 (bodies, clothes, hair, head)
        private RarityType rarity;    // 등급 (common, rare, epic, legendary)
        private String path;          // 애셋 경로
        private Boolean isNew;        // 신규 획득 여부 (true: 처음, false: 중복)
    }
}
