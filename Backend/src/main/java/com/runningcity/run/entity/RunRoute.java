package com.runningcity.run.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "run_route")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunRoute {

    @Id
    private Long sessionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "session_id")
    private RunSession session;

    // geometry 는 네이티브 SQL에서만 관리
    @Column(name = "length_m")
    private Double lengthM;

    // DB 기본값/업서트가 관리하므로 읽기 전용으로 매핑
    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false, nullable = false)
    private Instant updatedAt;
}
