package com.runningcity.gps.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunningSessionResponse {
    private boolean valid;           // 검증 결과
    private double clientDistance;   // 클라이언트 거리(km)
    private double serverDistance;   // 서버 계산 거리(km)
    private double serverAvgPace;    // 서버 계산 페이스(min/km)
    private double serverAvgSpeed;   // 서버 계산 속도(km/h)
    private String message;          // 상태 메시지
}
