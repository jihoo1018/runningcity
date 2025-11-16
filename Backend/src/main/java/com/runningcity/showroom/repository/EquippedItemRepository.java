package com.runningcity.showroom.repository;


import com.runningcity.showroom.dto.UserEquippedItemResponse;
import com.runningcity.showroom.entity.UserEquippedItem;
import com.runningcity.showroom.entity.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EquippedItemRepository extends JpaRepository<UserEquippedItem, Long> {

    List<UserEquippedItem> findByUserId(Long userId);

    void deleteAllByUserId(Long userId);

    @Query("""
        SELECT new com.runningcity.showroom.dto.UserEquippedItemResponse(
                    u.equippedId,
                    u.itemId,
                    b.category,
                    b.subcategory,
                    b.style,
                    b.basePath
                )
                FROM UserEquippedItem u
                JOIN Boutique b ON u.itemId = b.itemId
                WHERE u.userId = :userId
    """)
    List<UserEquippedItemResponse> findEquippedItemsByUserId(Long userId);
}
