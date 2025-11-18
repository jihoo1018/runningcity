package com.runningcity.showroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyOfficeResponse {
    private double  totalDist;
    private double  maxDist;
    private double  avgPace;
    private double  bestPace;
    private Long totalEntryCnt;
    private List<UserEquippedItemResponse> equippedItemList;
    private PrivacySettingResponse privacySetting;

    // 🔄 Privacy 포함 버전 (신규)
    public static MyOfficeResponse create(
            double totalDist,
            double maxDist,
            double avgPace,
            double bestPace,
            Long totalEntryCnt,
            List<UserEquippedItemResponse> list,
            PrivacySettingResponse privacySetting  // 🆕 파라미터 추가
    ) {
        return MyOfficeResponse.builder()
                .totalDist(totalDist)
                .maxDist(maxDist)
                .avgPace(avgPace)
                .bestPace(bestPace)
                .totalEntryCnt(totalEntryCnt)
                .equippedItemList(list)
                .privacySetting(privacySetting)  // 🆕 추가
                .build();
    }

}
