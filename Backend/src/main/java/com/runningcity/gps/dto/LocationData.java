package com.runningcity.gps.dto;

import lombok.*;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationData {
    private double latitude; // 위도
    private double longitude;// 경도
    private long timestamp;// 측정 시각(epoch ms)

    private float accuracy; // GPS 정확도 (미터 단위) -> GPS 신호 품질 체크
    private double altitude; // 고도 (미터) -> 언덕 코스 감지, 난이도 분석
    private float speed;// 속도 (m/s) -> 러닝 속도 계산, 페이스 표시
    private float bearing; // 진행 방향 (북쪽 기준, °) -> 달리는 방향 화살표 표시
    private String provider; // 위치 제공자(GPS, network 등) -> GPS / 네트워크 기반 위치 구분

}
