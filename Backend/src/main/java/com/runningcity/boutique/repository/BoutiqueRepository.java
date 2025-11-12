package com.runningcity.boutique.repository;

import com.runningcity.boutique.dto.StoreResponse;
import com.runningcity.boutique.entity.Boutique;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoutiqueRepository extends JpaRepository<Boutique, Long> {

    /**
     * obtainMethod가 "store"인 아이템 전체 조회
     */
    List<Boutique> findByObtainMethod(String obtainMethod);


}
