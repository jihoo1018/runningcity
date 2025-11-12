package com.runningcity.friendship.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddFriendRequest {

    @NotBlank(message = "친구 코드는 필수입니다.")
    private String friendCode;
}

