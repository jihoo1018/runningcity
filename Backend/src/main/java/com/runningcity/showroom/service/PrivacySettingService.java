package com.runningcity.showroom.service;

import com.runningcity.showroom.dto.PrivacySettingRequest;
import com.runningcity.showroom.dto.PrivacySettingResponse;
import com.runningcity.showroom.entity.PrivacySetting;
import com.runningcity.showroom.entity.UserTag;
import com.runningcity.showroom.mapper.PrivacySettingMapper;
import com.runningcity.showroom.repository.PrivacySettingRepository;
import com.runningcity.showroom.repository.UserTagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrivacySettingService {

    private final PrivacySettingRepository settingRepo;
    private final UserTagRepository tagRepo;
    private final PrivacySettingMapper mapper;

    @Transactional
    public void savePrivacySetting(Long userId, PrivacySettingRequest req) {

        // PrivacySetting upsert
        PrivacySetting setting = settingRepo.findByUserId(userId)
                .orElseGet(() -> mapper.toEntity(userId, req));

        mapper.updateEntity(setting, req);
        settingRepo.save(setting);

        // Tags 업데이트
        tagRepo.deleteAllByUserId(userId);

        req.getTags().forEach(tag -> {
            UserTag userTag = UserTag.create(userId, tag);
            tagRepo.save(userTag);
        });
    }

    // 내 사무실 설정 조회(데이터 없어도 무조건 response dto 리턴됨 -> 없을 경우 default response dto 생성해서 리턴)
    public PrivacySettingResponse getPrivacySetting(Long userId) {
        // 태그 조회
        List<UserTag> tags = tagRepo.findAllByUserId(userId);

        return settingRepo.findByUserId(userId)
                .map(setting -> PrivacySettingResponse.fromEntity(setting, tags))
                .orElseGet(() -> PrivacySettingResponse.createDefault(userId, tags));
    }
}
