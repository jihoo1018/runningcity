package com.runningcity.boutique.service;

import com.runningcity.showroom.repository.ShowRoomRepository;
import com.runningcity.boutique.dto.StoreResponse;
import com.runningcity.boutique.entity.Boutique;
import com.runningcity.boutique.repository.BoutiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BoutiqueService {

    private final BoutiqueRepository boutiqueRepository;
    private final ShowRoomRepository showRoomRepository;

    /**
     * 스토어 아이템 전체 목록 조회 (구매 여부 포함)
     *
     * @param userId 현재 사용자 ID
     * @return obtainMethod = "store"인 모든 아이템 목록
     */
    @Transactional(readOnly = true)
    public List<StoreResponse> getStoreItems(Long userId) {
        // 1 obtainMethod = "store"인 모든 아이템 조회
        List<Boutique> storeItems = boutiqueRepository
                .findByObtainMethod("store");
        // 2️ 사용자가 구매한 아이템 ID 목록 조회
        Set<Long> purchasedItemIds = showRoomRepository
                .findPurchasedItemIds(userId);
        // 3️ DTO 변환 (구매 여부 포함)
        return storeItems
                .stream()
                .map(item -> StoreResponse.from(
                        item, purchasedItemIds.contains(item.getItemId())
                ))
                .toList();
    }




}
