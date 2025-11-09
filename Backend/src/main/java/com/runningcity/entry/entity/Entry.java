package com.runningcity.entry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="entry")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entry { // 잠입 기지

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "base_id")
    private Long baseId;

    @Column(name="course_nm")
    private String courseNm; // 코스명

    @Column(name="course_desc")
    private String courseDesc; // 코스 설명

    private String region; // 지역

    @Column(name="distance_km")
    private double distanceKm; // 거리 km

    private String difficulty; // 난이도

    private String duration; // 소요시간

    private String address; // 주소

    private Double latitude; // 위도

    private Double longitude; // 경도

    @Column(name = "data_source")
    private String dataSource; // 데이터 출처

    @Column(name = "group_no")
    private Integer groupNo; // 그룹 번호
    
}
