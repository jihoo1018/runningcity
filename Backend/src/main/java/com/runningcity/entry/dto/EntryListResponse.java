package com.runningcity.entry.dto;

import com.runningcity.entry.entity.Entry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EntryListResponse {
    private Long baseId; // id
    private String courseNm; // 코스명
    private String region; // 지역
    private Double latitude; // 위도
    private Double longitude; // 경도
    private Integer groupNo; // 그룹 번호


    // Entity → DTO 변환 (JPA에서 가져온 Entity를 Response로 매핑)
    public static EntryListResponse fromEntity(Entry entity) {
        return EntryListResponse.builder()
                .baseId(entity.getBaseId())
                .courseNm(entity.getCourseNm())
                .region(entity.getRegion())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .groupNo(entity.getGroupNo())
                .build();
    }
}
