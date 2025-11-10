package com.runningcity.report.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "run_session")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "avg_heart_rate")
    private Integer avgHeartRate;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "avg_pace")
    private Integer avgPace;

    @Column(name = "rewards_meta")
    private String rewardsMeta;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public Double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Double totalDistance) { this.totalDistance = totalDistance; }

    public Integer getAvgHeartRate() { return avgHeartRate; }
    public void setAvgHeartRate(Integer avgHeartRate) { this.avgHeartRate = avgHeartRate; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getAvgPace() { return avgPace; }
    public void setAvgPace(Integer avgPace) { this.avgPace = avgPace; }

    public String getRewardsMeta() { return rewardsMeta; }
    public void setRewardsMeta(String rewardsMeta) { this.rewardsMeta = rewardsMeta; }
}
