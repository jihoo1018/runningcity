package com.runningcity.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.runningcity.user.entity.User;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByGoogleId(String googleId);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByGoogleId(String googleId);
    
    boolean existsByEmail(String email);
    
    // 닉네임 중복 체크
    boolean existsByNickname(String nickname);
    
    // 특정 사용자를 제외한 닉네임 중복 체크
    boolean existsByNicknameAndUserIdNot(String nickname, Long userId);
    
    // userCode로 사용자 조회
    Optional<User> findByUserCode(String userCode);

    // ===== 추가: EXP/크레딧 누적 업데이트 =====
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE users
           SET total_exp    = COALESCE(total_exp, 0) + :gainedExp,
               total_credit = COALESCE(total_credit, 0) + :gainedCredit,
               updated_at   = NOW()
         WHERE user_id = :userId
        """, nativeQuery = true)
    int addExpAndCredit(@Param("userId") Long userId,
                        @Param("gainedExp") long gainedExp,
                        @Param("gainedCredit") long gainedCredit);

    /**
     * 🎲 장착 아이템이 있는 랜덤 유저 조회 (친구 제외)
     *
     * @param excludedUserIds 제외할 유저 ID 목록
     * @param pageable 페이징 정보
     * @return 장착 아이템이 있는 랜덤 유저 목록
     */
    @Query(value = """
    SELECT u.*
    FROM users u
    WHERE EXISTS (
        SELECT 1 
        FROM user_equipped_items uei 
        WHERE uei.user_id = u.user_id
    )
    AND u.user_id NOT IN (:excludedUserIds)
    AND u.is_active = true
    ORDER BY RANDOM()
    LIMIT :limit
    """, nativeQuery = true)
    List<User> findRandomUsersWithEquippedItems(
            @Param("excludedUserIds") List<Long> excludedUserIds,
            @Param("limit") int limit
    );
    /**
     * 🎯 특정 유저 ID 목록에 해당하는 유저들 조회 (레벨 높은 순)
     *
     * 용도: 친구 쇼룸
     *
     * @param userIds 조회할 유저 ID 목록
     * @return 레벨 내림차순으로 정렬된 유저 목록
     */
    @Query("""
    SELECT u FROM User u
    WHERE u.userId IN :userIds
    ORDER BY u.level DESC, u.userId ASC
""")
    List<User> findByUserIdInOrderByLevelDesc(@Param("userIds") List<Long> userIds);

    /**
     * 🎯 특정 유저 ID 목록에 해당하는 유저들 조회 (레벨 높은 순, 개수 제한)
     *
     * @param userIds 조회할 유저 ID 목록
     * @param pageable 조회 개수 제한
     * @return 레벨 내림차순으로 정렬된 유저 목록
     */
    @Query("""
    SELECT u FROM User u
    WHERE u.userId IN :userIds
    ORDER BY u.level DESC, u.userId ASC
""")
    List<User> findByUserIdInOrderByLevelDesc(
            @Param("userIds") List<Long> userIds,
            Pageable pageable
    );
}

