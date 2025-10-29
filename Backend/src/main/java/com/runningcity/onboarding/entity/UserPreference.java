package com.runningcity.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.runningcity.user.entity.User;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @Setter
    private User user;

    @Column(name = "has_running_history", nullable = false)
    private Boolean hasRunningHistory;

    @Enumerated(EnumType.STRING)
    @Column(name = "fitness_level", nullable = false, length = 20)
    private FitnessLevel fitnessLevel;

    @Column(name = "target_distance_km", nullable = false)
    private Float targetDistanceKm;

    @Column(name = "resting_heart_rate")
    private Integer restingHeartRate;

    @Column(name = "has_smart_watch", nullable = false)
    private Boolean hasSmartWatch;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public enum FitnessLevel {
        BEGINNER, // 입문자
        INTERMEDIATE, // 초급자
        ADVANCED, // 중급자
        EXPERT, // 상급자
        ELITE // 전문가
    }
}

