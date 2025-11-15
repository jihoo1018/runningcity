package com.runningcity.showroom.dto;

import com.runningcity.entry.dto.EntryDetailResponse;
import com.runningcity.entry.entity.Entry;
import com.runningcity.showroom.entity.PrivacySetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingResponse {
    private Long userId;
    private boolean isGlobalPublic;
    private boolean showTotalRunning;
    private boolean showMaxDistance;
    private boolean showAvgPace;
    private boolean showBestPace;
    private boolean showHikingCount;

    private List<String> tags; // 최대 4개

    public static PrivacySettingResponse fromEntity(PrivacySetting entity) {
        return PrivacySettingResponse.builder()
                .userId(entity.getUserId())
                .isGlobalPublic(entity.isGlobalPublic())
                .showTotalRunning(entity.isShowTotalRunning())
                .showMaxDistance(entity.isShowMaxDistance())
                .showAvgPace(entity.isShowAvgPace())
                .showBestPace(entity.isShowBestPace())
                .showHikingCount(entity.isShowHikingCount())
                .build();
    }
}
