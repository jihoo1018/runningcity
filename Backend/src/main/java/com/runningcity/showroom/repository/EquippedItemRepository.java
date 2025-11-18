package com.runningcity.showroom.repository;

import com.runningcity.showroom.dto.UserEquippedItemResponse;
import com.runningcity.showroom.entity.UserEquippedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquippedItemRepository extends JpaRepository<UserEquippedItem, Long> {

    List<UserEquippedItem> findByUserId(Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_equipped_items WHERE user_id = :userId", nativeQuery = true)
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT new com.runningcity.showroom.dto.UserEquippedItemResponse(
            u.equippedId,
            u.itemId,
            b.category,
            b.subcategory,
            b.style,
            b.basePath,
            'COMPOSITE'
        )
        FROM UserEquippedItem u
        JOIN Boutique b ON u.itemId = b.itemId
        WHERE u.userId = :userId
        ORDER BY 
            CASE b.category
                WHEN 'BODIES' THEN 1
                WHEN 'CLOTHES' THEN 2
                WHEN 'HAIR' THEN 3
                WHEN 'HEAD' THEN 4
            END,
            b.subcategory
    """)
    List<UserEquippedItemResponse> findEquippedItemsByUserId(Long userId);

    /**
     * 용도: 랜덤 아바타/친구 아바타 목록 조회 시 N+1 문제 방지
     * 동작:
     * - 한 번의 쿼리로 여러 유저의 장착 아이템 조회
     * - Service에서 userId별로 그룹핑하여 사용
     * 예시:
     * - userIds = [5, 10, 15]
     * - 결과: userId가 5, 10, 15인 모든 장착 아이템 반환
     * @param userIds 조회할 유저 ID 목록
     * @return 해당 유저들의 모든 장착 아이템 목록
     */
    List<UserEquippedItem> findAllByUserIdIn(List<Long> userIds);
}