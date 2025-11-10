package com.runningcity.entry.dto;

import com.runningcity.entry.entity.Entry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EntryDetailResponse {
    private Long baseId; // id
    private String courseNm; // 코스명
    private String courseDesc; // 코스 설명
    private String region; // 지역
    private double distanceKm; // 거리 km
    private String difficulty; // 난이도
    private String duration; // 소요시간
    private String address; // 주소
    private Double latitude; // 위도
    private Double longitude; // 경도
    private String dataSource; // 데이터 출처
    private Integer groupNo; // 그룹 번호

    // Entity → DTO 변환 (JPA에서 가져온 Entity를 Response로 매핑)
    public static EntryDetailResponse fromEntity(Entry entity) {
        return EntryDetailResponse.builder()
                .baseId(entity.getBaseId())
                .courseNm(entity.getCourseNm())
                .courseDesc(entity.getCourseDesc())
                .region(entity.getRegion())
                .distanceKm(entity.getDistanceKm())
                .difficulty(entity.getDifficulty())
                .duration(entity.getDuration())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .dataSource(entity.getDataSource())
                .groupNo(entity.getGroupNo())
                .build();
    }
}
