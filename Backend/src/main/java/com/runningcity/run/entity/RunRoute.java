package com.runningcity.run.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity @Table(name="run_route")
@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RunRoute {
    @Id
    private Long sessionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name="session_id")
    private RunSession session;

    // geometry 컬럼은 네이티브 SQL로 채움 (JPA 필드 생략 가능)
    @Column(name="length_m")
    private Double lengthM;

    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @PrePersist
    void prePersist(){
        if (createdAt == null) createdAt = Instant.now();
    }
}
