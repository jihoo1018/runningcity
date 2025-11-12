package com.runningcity.mission.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserAccountWriteRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public int addExpAndCredit(Long userId, long gainedExp, long gainedCredit) {
        return em.createNativeQuery("""
                UPDATE users
                SET
                    total_exp = COALESCE(total_exp, 0) + :gainedExp,
                    total_credit = COALESCE(total_credit, 0) + :gainedCredit
                WHERE user_id = :userId
                """)
                .setParameter("userId", userId)
                .setParameter("gainedExp", gainedExp)
                .setParameter("gainedCredit", gainedCredit)
                .executeUpdate();
    }
}
