// src/main/java/com/runningcity/mission/repository/UserAccountWriteRepository.java
package com.runningcity.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.runningcity.user.entity.User;

@Repository
public interface UserAccountWriteRepository extends JpaRepository<User, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        update users
           set total_exp    = coalesce(total_exp, 0) + :gainedExp,
               total_credit = coalesce(total_credit, 0) + :gainedCredit,
               updated_at   = now()
         where user_id = :userId
        """, nativeQuery = true)
    int addExpAndCredit(@Param("userId") Long userId,
                        @Param("gainedExp") long gainedExp,
                        @Param("gainedCredit") long gainedCredit);
}
