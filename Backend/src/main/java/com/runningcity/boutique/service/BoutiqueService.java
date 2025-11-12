package com.runningcity.boutique.service;

import com.runningcity.boutique.dto.PurchaseRequest;
import com.runningcity.boutique.dto.PurchaseResponse;
import com.runningcity.boutique.exception.BoutiqueResponseCode;
import com.runningcity.global.exception.BaseException;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.showroom.repository.ShowRoomRepository;
import com.runningcity.boutique.dto.StoreResponse;
import com.runningcity.boutique.entity.Boutique;
import com.runningcity.boutique.repository.BoutiqueRepository;
import com.runningcity.showroom.service.ShowRoomService;
import com.runningcity.user.entity.User;
import com.runningcity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoutiqueService {

    private final BoutiqueRepository boutiqueRepository;
    private final ShowRoomRepository showRoomRepository;
    private final UserRepository userRepository;
    private final ShowRoomService showRoomService;

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

    @Transactional
    public PurchaseResponse purchaseItem(Long userId ,PurchaseRequest request) {
        Long itemId = request.getItemId();

        // 1️ 상품 존재 여부 확인
        Boutique item = boutiqueRepository.findById(itemId)
                .orElseThrow(() -> new BaseException(BoutiqueResponseCode.ITEM_NOT_FOUND));
        // 2️ 상점 구매 가능 상품인지 확인
        if (!"store".equals(item.getObtainMethod())) {
            throw new BaseException(BoutiqueResponseCode.NOT_STORE_ITEM);
        }
        // 3 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.USER_NOT_FOUND));
        // 4 크레딧 부족 확인
        if (user.getTotalCredit() < item.getPriceCr()) {
            throw new BaseException(BoutiqueResponseCode.INSUFFICIENT_CREDITS);
        }
        // 5 크레딧 차감
        user.deductCredit(item.getPriceCr());
        long leftCredit = user.getTotalCredit();

        // 6 인벤토리에 신규 추가
        showRoomService.addItem(userId, itemId);

        // 7 응답 DTO 생성
        return PurchaseResponse.from(item, leftCredit);
    }



}
