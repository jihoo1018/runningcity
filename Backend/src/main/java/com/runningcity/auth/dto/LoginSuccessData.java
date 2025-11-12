package com.runningcity.auth.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginSuccessData {
    private Long userId;
    private String userNickname;
    private String userCode;
    private Long totalexp; // DB total_exp 매핑 주의
}