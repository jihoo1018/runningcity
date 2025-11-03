package com.runningcity.run.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSessionRequest {
    @NotBlank(message = "세션 유형은 필수입니다.")
    private String type;        // "NORMAL" | "ENTRY"

    @NotBlank(message = "디바이스 유형은 필수입니다.")
    private String deviceType;  // "ANDROID_PHONE" | "WEAR_OS"

    // 선택
    private Long baseId;
}