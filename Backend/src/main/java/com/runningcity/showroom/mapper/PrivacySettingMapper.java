package com.runningcity.showroom.mapper;

import com.runningcity.showroom.dto.PrivacySettingRequest;
import com.runningcity.showroom.entity.PrivacySetting;
import org.springframework.stereotype.Component;

@Component
public class PrivacySettingMapper {

    public PrivacySetting toEntity(Long userId, PrivacySettingRequest req) {
        return PrivacySetting.create(
                userId,
                req.isGlobalPublic(),
                req.isShowTotalRunning(),
                req.isShowMaxDistance(),
                req.isShowAvgPace(),
                req.isShowBestPace(),
                req.isShowHikingCount()
        );
    }

    public void updateEntity(PrivacySetting entity, PrivacySettingRequest req) {
        entity.update(
                req.isGlobalPublic(),
                req.isShowTotalRunning(),
                req.isShowMaxDistance(),
                req.isShowAvgPace(),
                req.isShowBestPace(),
                req.isShowHikingCount()
        );
    }
}
