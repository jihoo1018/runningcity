package com.runningcity.entry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntrySearchRequest {
    private Integer groupNo;     // 그룹 번호로 필터
    private String region;       // 지역 필터
    private String difficulty;   // 난이도 필터
    private Double minDistance;  // 최소 거리
    private Double maxDistance;  // 최대 거리
}
