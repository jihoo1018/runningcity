package com.runningcity.friendship.dto;

import com.runningcity.friendship.entity.Friendship;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class FriendshipResponse {
    private Long friendshipId;
    private Long requesterId;
    private Long addresseeId;
    private String status;
    private ZonedDateTime createdAt;

    public static FriendshipResponse from(Friendship friendship) {
        return FriendshipResponse.builder()
                .friendshipId(friendship.getFriendshipId())
                .requesterId(friendship.getRequester().getUserId())
                .addresseeId(friendship.getAddressee().getUserId())
                .status(friendship.getStatus().name())
                .createdAt(friendship.getCreatedAt())
                .build();
    }
}

