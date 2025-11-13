package com.runningcity.user.entity;

import com.runningcity.global.exception.BaseException;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.onboarding.entity.UserPreference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "user_code", columnDefinition = "TEXT")
    private String userCode;

    @Column(name = "has_completed_onboarding", nullable = false)
    @Builder.Default
    private Boolean hasCompletedOnboarding = false;

    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "total_exp", nullable = false)
    @Builder.Default
    private Long totalExp = 0L;

    @Column(name = "total_credit", nullable = false)
    @Builder.Default
    private Long totalCredit = 0L;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserPreference userPreference;

    // 비즈니스 메서드
    public void completeOnboarding() {
        this.hasCompletedOnboarding = true;
    }

    public void setUserPreference(UserPreference userPreference) {
        this.userPreference = userPreference;
        if (userPreference != null) {
            userPreference.setUser(this);
        }
    }

    /**
     * 닉네임 수정
     * @param nickname 새로운 닉네임
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // ============================================
    // ✅ 크레딧 관련 비즈니스 메서드 (추가)
    // ============================================

    /**
     * 크레딧 차감 (상점 구매 시)
     * @param amount 차감할 크레딧
     * @throws IllegalArgumentException 크레딧 부족 시
     */
    public void deductCredit(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new BaseException(CommonResponseCode.INVALID_CREDIT_AMOUNT);
        }
        if (this.totalCredit < amount) {
            throw new BaseException(CommonResponseCode.INSUFFICIENT_CREDIT);
        }
        this.totalCredit -= amount;
    }

    /**
     * 크레딧 충분 여부 확인
     * @param amount 필요한 크레딧
     * @return 구매 가능 여부
     */
    public boolean hasEnoughCredit(Long amount) {
        return this.totalCredit >= amount;
    }
}

