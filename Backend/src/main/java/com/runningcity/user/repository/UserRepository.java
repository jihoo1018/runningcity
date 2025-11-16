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
     * 🎯 친구가 아닌 유저들 랜덤 조회
     * 용도: 랜덤 아바타 쇼룸
     * @param excludedUserIds 제외할 유저 ID 목록 (친구 + 본인)
     * @param pageable 조회 개수 제한 (예: PageRequest.of(0, 10))
     * @return 랜덤 순서로 정렬된 유저 목록
     */
    @Query(value = """
        SELECT * FROM users u
        WHERE u.user_id NOT IN (:excludedUserIds)
        ORDER BY RANDOM()
        """,
            nativeQuery = true)
    List<User> findRandomNonFriends(
            @Param("excludedUserIds") List<Long> excludedUserIds,
            Pageable pageable
    );
}

