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
public class PrivacySettingRequest {
    private boolean isGlobalPublic;
    private boolean showTotalRunning;
    private boolean showMaxDistance;
    private boolean showAvgPace;
    private boolean showBestPace;
    private boolean showHikingCount;

    private List<String> tags; // TODO 유저당 최대 4개의 태그 제약하기
}
