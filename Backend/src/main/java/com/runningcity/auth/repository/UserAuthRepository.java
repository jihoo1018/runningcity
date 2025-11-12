package com.runningcity.auth.repository;

import com.runningcity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUserCode(String userCode);
    Optional<User> findByEmail(String email);
}
