package com.runningcity.friendship.dto;

import com.runningcity.friendship.entity.Friendship;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class GetSentRequestsResponse {
    private Long friendshipId;
    private Long friendId;
    private String friendNickname;
    private String friendProfileImageUrl;
    private Integer friendLevel;
    private ZonedDateTime createdAt;

    public static GetSentRequestsResponse from(Friendship friendship) {
        return GetSentRequestsResponse.builder()
                .friendshipId(friendship.getFriendshipId())
                .friendId(friendship.getAddressee().getUserId())
                .friendNickname(friendship.getAddressee().getNickname())
                .friendProfileImageUrl(friendship.getAddressee().getProfileImageUrl())
                .friendLevel(friendship.getAddressee().getLevel())
                .createdAt(friendship.getCreatedAt())
                .build();
    }
}

