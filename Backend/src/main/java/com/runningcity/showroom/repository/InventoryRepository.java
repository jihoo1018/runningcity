package com.runningcity.showroom.repository;


import com.runningcity.showroom.entity.UserEquippedItem;
import com.runningcity.showroom.entity.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InventoryRepository extends JpaRepository<UserInventory, Long> {

    /**
     * 특정 유저가 구매한 아이템 ID 목록 조회
     * StoreResponse의 isPurchased 판단에 사용
     *
     * @param userId 사용자 ID
     * @return 구매한 아이템 ID Set (중복 없음)
     */
    @Query("SELECT ui.itemId FROM UserInventory ui WHERE ui.userId = :userId")
    Set<Long> findPurchasedItemIds(@Param("userId") Long userId);

    // 중복 구매 체크
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    /**
     * 유저의 특정 아이템 조회
     */
    Optional<UserInventory> findByUserIdAndItemId(Long userId, Long itemId);

    List<UserInventory> findByUserId(Long userId);
}
