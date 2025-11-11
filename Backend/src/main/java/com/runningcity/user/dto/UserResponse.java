package com.runningcity.user.dto;

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
}

