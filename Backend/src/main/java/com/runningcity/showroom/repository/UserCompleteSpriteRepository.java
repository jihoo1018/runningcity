package com.runningcity.showroom.repository;

import com.runningcity.showroom.entity.UserCompleteSprite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCompleteSpriteRepository extends JpaRepository<UserCompleteSprite, Long> {
    Optional<UserCompleteSprite> findByUserId(Long userId);
}
