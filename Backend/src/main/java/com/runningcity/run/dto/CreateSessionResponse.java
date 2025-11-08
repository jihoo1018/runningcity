package com.runningcity.run.dto;

import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSessionResponse {
    private Long sessionId;
}