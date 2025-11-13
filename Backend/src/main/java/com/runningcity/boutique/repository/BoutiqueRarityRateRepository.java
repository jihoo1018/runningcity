package com.runningcity.boutique.repository;

import com.runningcity.boutique.entity.BoutiqueRarityRate;
import com.runningcity.boutique.enums.RarityType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoutiqueRarityRateRepository extends JpaRepository<BoutiqueRarityRate, RarityType> {
    // 기본 CRUD만 사용
}