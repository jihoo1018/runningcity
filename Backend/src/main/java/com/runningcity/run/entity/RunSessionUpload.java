package com.runningcity.run.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="run_session_upload")
@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RunSessionUpload {
    @Id
    private Long sessionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name="session_id")
    private RunSession session;


    @Column(name = "acked_until_seq", nullable = false)
    @Builder.Default
    private Integer ackedUntilSeq = 0;

    @PrePersist
    void prePersist() {
        if (ackedUntilSeq == null) ackedUntilSeq = 0;
    }
}
