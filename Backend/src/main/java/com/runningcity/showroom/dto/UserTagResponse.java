package com.runningcity.showroom.dto;

import com.runningcity.showroom.entity.PrivacySetting;
import com.runningcity.showroom.entity.UserTag;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTagResponse {
    private Long id;
    private Long userId;
    private String tagName;

    public static UserTagResponse fromEntity(UserTag entity) {
        return UserTagResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .tagName(entity.getTagName())
                .build();
    }
}
