package com.runningcity.friendship.entity;

import com.runningcity.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "friendship")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friendship_id")
    private Long friendshipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    // 비즈니스 메서드
    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
    }

    public void reject() {
        this.status = FriendshipStatus.REJECTED;
    }

    public void cancel() {
        this.status = FriendshipStatus.CANCELLED;
    }

    public void block() {
        this.status = FriendshipStatus.BLOCKED;
    }

    public enum FriendshipStatus {
        PENDING,    // 대기 중
        ACCEPTED,  // 수락됨
        REJECTED,  // 거절됨
        CANCELLED,  // 취소됨
        BLOCKED     // 차단됨
    }
}

