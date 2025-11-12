package com.runningcity.friendship.dto;

import com.runningcity.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendRankingResponse {
    private Integer rank;              // 순위 (1등, 2등 등)
    private Long userId;               // 사용자 ID
    private String nickname;           // 닉네임
    private String profileImageUrl;    // 프로필 이미지 URL
    private Integer level;             // 레벨
    private Long totalExp;             // 총 경험치
    private Boolean isMe;              // 현재 사용자 여부

    public static FriendRankingResponse from(User user, Integer rank, Boolean isMe) {
        return FriendRankingResponse.builder()
                .rank(rank)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .level(user.getLevel())
                .totalExp(user.getTotalExp())
                .isMe(isMe)
                .build();
    }
}

