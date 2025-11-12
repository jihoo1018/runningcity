package com.runningcity.friendship.repository;

import com.runningcity.friendship.entity.Friendship;
import com.runningcity.friendship.entity.Friendship.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // 특정 사용자에게 보낸 요청 조회
    Optional<Friendship> findByRequesterUserIdAndAddresseeUserId(Long requesterId, Long addresseeId);

    // 보낸 요청 목록 조회 (PENDING 상태만)
    @Query("SELECT f FROM Friendship f " +
           "WHERE f.requester.userId = :userId " +
           "AND f.status = :status " +
           "ORDER BY f.createdAt DESC")
    List<Friendship> findSentRequestsByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") FriendshipStatus status
    );

    // 받은 요청 목록 조회 (PENDING 상태만)
    @Query("SELECT f FROM Friendship f " +
           "WHERE f.addressee.userId = :userId " +
           "AND f.status = :status " +
           "ORDER BY f.createdAt DESC")
    List<Friendship> findReceivedRequestsByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") FriendshipStatus status
    );

    // 친구 목록 조회 (ACCEPTED 상태만, 양방향)
    @Query("SELECT f FROM Friendship f " +
           "WHERE (f.requester.userId = :userId OR f.addressee.userId = :userId) " +
           "AND f.status = 'ACCEPTED' " +
           "ORDER BY f.updatedAt DESC")
    List<Friendship> findFriendsByUserId(@Param("userId") Long userId);

    // 이미 친구인지 확인
    @Query("SELECT COUNT(f) > 0 FROM Friendship f " +
           "WHERE ((f.requester.userId = :userId1 AND f.addressee.userId = :userId2) " +
           "OR (f.requester.userId = :userId2 AND f.addressee.userId = :userId1)) " +
           "AND f.status = 'ACCEPTED'")
    boolean existsFriendship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 양방향 친구 관계 조회 (친구 삭제용)
    @Query("SELECT f FROM Friendship f " +
           "WHERE ((f.requester.userId = :userId1 AND f.addressee.userId = :userId2) " +
           "OR (f.requester.userId = :userId2 AND f.addressee.userId = :userId1)) " +
           "AND f.status = 'ACCEPTED'")
    List<Friendship> findFriendshipBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );

    // 친구 목록 조회 (ACCEPTED 상태만, total_exp 기준 내림차순 정렬)
    @Query("SELECT u FROM User u " +
           "WHERE u.userId IN (" +
           "  SELECT CASE " +
           "    WHEN f.requester.userId = :userId THEN f.addressee.userId " +
           "    ELSE f.requester.userId " +
           "  END " +
           "  FROM Friendship f " +
           "  WHERE (f.requester.userId = :userId OR f.addressee.userId = :userId) " +
           "  AND f.status = 'ACCEPTED'" +
           ") " +
           "ORDER BY u.totalExp DESC, u.userId ASC")
    List<com.runningcity.user.entity.User> findFriendsByUserIdOrderByTotalExp(@Param("userId") Long userId);
}

