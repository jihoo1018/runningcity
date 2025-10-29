package com.runningcity.onboarding.repository;

import com.runningcity.onboarding.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    
    Optional<UserPreference> findByUser_UserId(Long userId);
    
    boolean existsByUser_UserId(Long userId);
}

