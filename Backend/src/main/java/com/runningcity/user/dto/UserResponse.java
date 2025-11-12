package com.runningcity.user.dto;

import com.runningcity.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long userId;
    private String googleId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private Boolean hasCompletedOnboarding;
    private Integer level;
    private Long totalExp;
    private Long totalCredit;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .googleId(user.getGoogleId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .hasCompletedOnboarding(user.getHasCompletedOnboarding())
                .level(user.getLevel())
                .totalExp(user.getTotalExp())
                .totalCredit(user.getTotalCredit())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

