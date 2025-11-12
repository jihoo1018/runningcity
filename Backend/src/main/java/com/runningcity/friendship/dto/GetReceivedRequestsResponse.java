package com.runningcity.friendship.dto;

import com.runningcity.friendship.entity.Friendship;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class GetReceivedRequestsResponse {
    private Long friendshipId;
    private Long requesterId;
    private String requesterNickname;
    private String requesterProfileImageUrl;
    private Integer requesterLevel;
    private ZonedDateTime createdAt;

    public static GetReceivedRequestsResponse from(Friendship friendship) {
        return GetReceivedRequestsResponse.builder()
                .friendshipId(friendship.getFriendshipId())
                .requesterId(friendship.getRequester().getUserId())
                .requesterNickname(friendship.getRequester().getNickname())
                .requesterProfileImageUrl(friendship.getRequester().getProfileImageUrl())
                .requesterLevel(friendship.getRequester().getLevel())
                .createdAt(friendship.getCreatedAt())
                .build();
    }
}

