package com.runningcity.showroom.repository;


import com.runningcity.showroom.entity.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserTagRepository extends JpaRepository<UserTag, Long> {

    int deleteAllByUserId(Long userId);
}
