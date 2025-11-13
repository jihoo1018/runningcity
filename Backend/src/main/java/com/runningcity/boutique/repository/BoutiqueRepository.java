package com.runningcity.boutique.repository;

import com.runningcity.boutique.entity.Boutique;
import com.runningcity.boutique.enums.ObtainMethod;
import com.runningcity.boutique.enums.RarityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoutiqueRepository extends JpaRepository<Boutique, Long> {

    /**
     * obtainMethod가 "store"인 아이템 전체 조회
     */
    List<Boutique> findByObtainMethod(ObtainMethod obtainMethod);

    @Query("""
        SELECT b FROM Boutique b
        WHERE b.obtainMethod = 'GACHA'
        AND b.rarity = :rarity
    """)
    List<Boutique> findGachaItemsByRarity(@Param("rarity") RarityType rarity);


}
