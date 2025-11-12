package com.runningcity.mission.repository;

import com.runningcity.onboarding.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceReadRepository extends JpaRepository<UserPreference, Long> {

    
    Optional<UserPreference> findByUser_UserId(Long userId);
}
