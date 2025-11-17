package com.runningcity.showroom.mapper;

import com.runningcity.showroom.dto.PrivacySettingRequest;
import com.runningcity.showroom.dto.UserEquippedItemRequest;
import com.runningcity.showroom.entity.PrivacySetting;
import com.runningcity.showroom.entity.UserEquippedItem;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class UserEquippedItemMapper {

    public UserEquippedItem toEntity(Long userId, UserEquippedItemRequest req) {
        return UserEquippedItem.create(
                userId,
                req.getItemId(),
                req.getCategory(),
                req.getSubcategory(),
                req.getStyle(),
                ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        );
    }
}
