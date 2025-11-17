package com.runningcity.showroom.dto;

import com.runningcity.entry.dto.EntryDetailResponse;
import com.runningcity.entry.entity.Entry;
import com.runningcity.showroom.entity.PrivacySetting;
import com.runningcity.showroom.entity.UserTag;
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
    private boolean globalPublic;
    private boolean showTotalRunning;
    private boolean showMaxDistance;
    private boolean showAvgPace;
    private boolean showBestPace;
    private boolean showHikingCount;

    private List<String> tags; // 최대 4개

    public static PrivacySettingResponse fromEntity(PrivacySetting entity,
                                                    List<UserTag> userTags) {
        return PrivacySettingResponse.builder()
                .userId(entity.getUserId())
                .globalPublic(entity.isGlobalPublic())
                .showTotalRunning(entity.isShowTotalRunning())
                .showMaxDistance(entity.isShowMaxDistance())
                .showAvgPace(entity.isShowAvgPace())
                .showBestPace(entity.isShowBestPace())
                .showHikingCount(entity.isShowHikingCount())
                .tags(
                        userTags.stream()
                                .map(UserTag::getTagName)
                                .toList()
                )
                .build();
    }

    public static PrivacySettingResponse createDefault(Long userId, List<UserTag> tags) {
        return PrivacySettingResponse.builder()
                .userId(userId)
                .globalPublic(true)
                .showTotalRunning(true)
                .showMaxDistance(true)
                .showAvgPace(true)
                .showBestPace(true)
                .showHikingCount(true)
                .tags(tags.stream().map(UserTag::getTagName).toList())
                .build();
    }

}
