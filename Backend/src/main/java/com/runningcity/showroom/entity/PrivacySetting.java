package com.runningcity.showroom.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "privacy_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrivacySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private boolean isGlobalPublic;
    private boolean showTotalRunning;
    private boolean showMaxDistance;
    private boolean showAvgPace;
    private boolean showBestPace;
    private boolean showHikingCount;

    // 정적 팩터리 메서드
    public static PrivacySetting create(
            Long userId,
            boolean isGlobalPublic,
            boolean showTotalRunning,
            boolean showMaxDistance,
            boolean showAvgPace,
            boolean showBestPace,
            boolean showHikingCount
    ) {
        PrivacySetting ps = new PrivacySetting();
        ps.userId = userId;
        ps.isGlobalPublic = isGlobalPublic;
        ps.showTotalRunning = showTotalRunning;
        ps.showMaxDistance = showMaxDistance;
        ps.showAvgPace = showAvgPace;
        ps.showBestPace = showBestPace;
        ps.showHikingCount = showHikingCount;

        return ps;
    }

    // 상태 업데이트 명령 메서드
    public void update(
            boolean isGlobalPublic,
            boolean showTotalRunning,
            boolean showMaxDistance,
            boolean showAvgPace,
            boolean showBestPace,
            boolean showHikingCount
    ) {
        this.isGlobalPublic = isGlobalPublic;
        this.showTotalRunning = showTotalRunning;
        this.showMaxDistance = showMaxDistance;
        this.showAvgPace = showAvgPace;
        this.showBestPace = showBestPace;
        this.showHikingCount = showHikingCount;
    }
}
