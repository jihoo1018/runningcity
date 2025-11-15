package com.runningcity.showroom.service;

import com.runningcity.showroom.dto.PrivacySettingRequest;
import com.runningcity.showroom.entity.PrivacySetting;
import com.runningcity.showroom.entity.UserTag;
import com.runningcity.showroom.mapper.PrivacySettingMapper;
import com.runningcity.showroom.repository.PrivacySettingRepository;
import com.runningcity.showroom.repository.UserTagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrivacySettingService {

    private final PrivacySettingRepository settingRepo;
    private final UserTagRepository tagRepo;
    private final PrivacySettingMapper mapper;

    @Transactional
    public void save(Long userId, PrivacySettingRequest req) {

        // 1) PrivacySetting upsert
        PrivacySetting setting = settingRepo.findByUserId(userId)
                .orElseGet(() -> mapper.toEntity(userId, req));

        mapper.updateEntity(setting, req);
        settingRepo.save(setting);

        // 2) Tags 업데이트
        tagRepo.deleteAllByUserId(userId);

        req.getTags().forEach(tag -> {
            UserTag userTag = UserTag.create(userId, tag);
            tagRepo.save(userTag);
        });
    }
}
