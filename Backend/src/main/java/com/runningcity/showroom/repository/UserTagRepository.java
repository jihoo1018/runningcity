package com.runningcity.showroom.repository;


import com.runningcity.showroom.entity.UserTag;
import com.runningcity.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserTagRepository extends JpaRepository<UserTag, Long> {
    List<UserTag> findAllByUserId(Long userId);
    int deleteAllByUserId(Long userId);
}
