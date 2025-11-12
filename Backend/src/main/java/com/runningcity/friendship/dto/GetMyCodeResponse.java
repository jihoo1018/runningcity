package com.runningcity.friendship.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetMyCodeResponse {
    private String userCode;
}

