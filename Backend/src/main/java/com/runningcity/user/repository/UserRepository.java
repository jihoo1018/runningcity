package com.runningcity.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.runningcity.user.entity.User;

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
}

