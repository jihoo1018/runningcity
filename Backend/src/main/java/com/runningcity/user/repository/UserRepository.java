package com.runningcity.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
}

