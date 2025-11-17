package com.runningcity.showroom.repository;


import com.runningcity.showroom.entity.UserTag;
import com.runningcity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface UserTagRepository extends JpaRepository<UserTag, Long> {
    List<UserTag> findAllByUserId(Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_tags WHERE user_id = :userId", nativeQuery = true)
    int deleteAllByUserId(Long userId);
}
