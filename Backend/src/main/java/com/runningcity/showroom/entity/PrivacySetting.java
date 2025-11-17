package com.runningcity.showroom.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "is_global_public")
    private boolean globalPublic;

    @Column(name = "show_total_running")
    private boolean showTotalRunning;

    @Column(name = "show_max_distance")
    private boolean showMaxDistance;

    @Column(name = "show_avg_pace")
    private boolean showAvgPace;

    @Column(name = "show_best_pace")
    private boolean showBestPace;

    @Column(name = "show_hiking_count")
    private boolean showHikingCount;


    // 정적 팩터리 메서드
    public static PrivacySetting create(
            Long userId,
            boolean globalPublic,
            boolean showTotalRunning,
            boolean showMaxDistance,
            boolean showAvgPace,
            boolean showBestPace,
            boolean showHikingCount
    ) {
        PrivacySetting ps = new PrivacySetting();
        ps.userId = userId;
        ps.globalPublic = globalPublic;
        ps.showTotalRunning = showTotalRunning;
        ps.showMaxDistance = showMaxDistance;
        ps.showAvgPace = showAvgPace;
        ps.showBestPace = showBestPace;
        ps.showHikingCount = showHikingCount;

        return ps;
    }

    // 상태 업데이트 명령 메서드
    public void update(
            boolean globalPublic,
            boolean showTotalRunning,
            boolean showMaxDistance,
            boolean showAvgPace,
            boolean showBestPace,
            boolean showHikingCount
    ) {
        this.globalPublic = globalPublic;
        this.showTotalRunning = showTotalRunning;
        this.showMaxDistance = showMaxDistance;
        this.showAvgPace = showAvgPace;
        this.showBestPace = showBestPace;
        this.showHikingCount = showHikingCount;
    }
}
