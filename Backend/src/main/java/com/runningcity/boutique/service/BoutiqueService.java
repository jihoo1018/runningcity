package com.runningcity.boutique.service;

import com.runningcity.boutique.dto.GachaRequest;
import com.runningcity.boutique.dto.PurchaseRequest;
import com.runningcity.boutique.dto.PurchaseResponse;
import com.runningcity.boutique.entity.GachaHistory;
import com.runningcity.boutique.enums.DrawType;
import com.runningcity.boutique.enums.ObtainMethod;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoutiqueService {

    private final BoutiqueRepository boutiqueRepository;
    private final ShowRoomRepository showRoomRepository;
    private final UserRepository userRepository;
    private final ShowRoomService showRoomService;

    //가챠용
    private static final long SINGLE_GACHA_PRICE = 50L;
    private static final long MULTI_GACHA_PRICE = 450L;

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
                .findByObtainMethod(ObtainMethod.store);
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
        if (!ObtainMethod.store.equals(item.getObtainMethod())) {
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

    /**
     * 가챠 뽑기
     */
//    @Transactional
//    public GachaResponse drawGacha(Long userId, GachaRequest request) {
//        DrawType drawType = request.getDrawType();
//
//        // 1️⃣ 타입에 따른 가격/수량 결정
//        long price = getGachaPrice(drawType);
//        int quantity = getGachaQuantity(drawType);
//
//        log.info("가챠 뽑기 시작: userId={}, drawType={}, quantity={}, price={}",
//                userId, drawType, quantity, price);
//
//        // 2️⃣ 사용자 조회
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new BaseException(CommonResponseCode.USER_NOT_FOUND));
//
//        // 3️⃣ 크레딧 확인 및 차감
//        if (user.getTotalCredit() < price) {
//            throw new BaseException(BoutiqueResponseCode.INSUFFICIENT_CREDITS);
//        }
//        user.deductCredit(price);
//
//        // 4️⃣ 가챠 아이템 풀 조회
//        List<Boutique> gachaPool = boutiqueRepository
//                .findByObtainMethod(ObtainMethod.gacha);
//
//        if (gachaPool.isEmpty()) {
//            throw new BaseException(BoutiqueResponseCode.NO_GACHA_ITEMS);
//        }
//
//        // 5️⃣ 랜덤 뽑기
//        UUID sessionId = UUID.randomUUID();
//        List<Boutique> drawnItems = drawRandomItems(gachaPool, quantity);
//
//        // 6️⃣ 가챠 이력 저장
//        List<GachaHistory> histories = drawnItems.stream()
//                .map(item -> GachaHistory.create(
//                        userId,
//                        item.getItemId(),
//                        item.getRarity(),
//                        drawType,
//                        sessionId
//                ))
//                .collect(Collectors.toList());
//        gachaHistoryRepository.saveAll(histories);
//
//        // 7️⃣ 인벤토리에 추가
//        List<GachaResponse.GachaItem> responseItems = new ArrayList<>();
//        for (Boutique item : drawnItems) {
//            boolean isNew = !showRoomService.hasItem(userId, item.getItemId());
//            showRoomService.addOrIncreaseItem(userId, item.getItemId(), 1);
//
//            responseItems.add(GachaResponse.GachaItem.builder()
//                    .itemId(item.getItemId())
//                    .itemName(item.getName())
//                    .category(item.getCategory())
//                    .rarity(item.getRarity())
//                    .path(item.getPath())
//                    .isNew(isNew)
//                    .build());
//        }
//
//        log.info("가챠 뽑기 완료: userId={}, drawType={}, sessionId={}, items={}",
//                userId, drawType, sessionId, drawnItems.size());
//
//        // 8️⃣ 응답 생성
//        return GachaResponse.builder()
//                .sessionId(sessionId)
//                .drawType(drawType)
//                .totalDraws(quantity)
//                .spentCredit(price)
//                .remainingCredit(user.getTotalCredit())
//                .items(responseItems)
//                .build();
//    }
//
//    /**
//     * DrawType에 따른 가격 반환
//     */
//    private long getGachaPrice(DrawType drawType) {
//        return switch (drawType) {
//            case single -> SINGLE_GACHA_PRICE;   // 50 CR
//            case multi -> MULTI_GACHA_PRICE;     // 450 CR
//        };
//    }
//
//    /**
//     * DrawType에 따른 뽑기 개수 반환
//     */
//    private int getGachaQuantity(DrawType drawType) {
//        return switch (drawType) {
//            case single -> 1;
//            case multi -> 10;
//        };
//    }
//
//    /**
//     * 랜덤 아이템 뽑기
//     */
//    private List<Boutique> drawRandomItems(List<Boutique> pool, int count) {
//        Random random = new Random();
//        List<Boutique> result = new ArrayList<>();
//
//        for (int i = 0; i < count; i++) {
//            Boutique item = drawSingleItem(pool, random);
//            result.add(item);
//        }
//
//        return result;
//    }
//
//    /**
//     * 단일 아이템 뽑기 (확률 기반)
//     */
//    private Boutique drawSingleItem(List<Boutique> pool, Random random) {
//        Map<RarityType, Double> weights = Map.of(
//                RarityType.common, 0.60,       // 60%
//                RarityType.rare, 0.25,         // 25%
//                RarityType.epic, 0.10,         // 10%
//                RarityType.legendary, 0.05     // 5%
//        );
//
//        double roll = random.nextDouble();
//        RarityType selectedRarity = determineRarity(roll, weights);
//
//        List<Boutique> itemsOfRarity = pool.stream()
//                .filter(item -> item.getRarity() == selectedRarity)
//                .collect(Collectors.toList());
//
//        if (itemsOfRarity.isEmpty()) {
//            return pool.get(random.nextInt(pool.size()));
//        }
//
//        return itemsOfRarity.get(random.nextInt(itemsOfRarity.size()));
//    }
//
//    /**
//     * 확률 기반 등급 결정
//     */
//    private RarityType determineRarity(double roll, Map<RarityType, Double> weights) {
//        double cumulative = 0.0;
//
//        for (RarityType rarity : RarityType.values()) {
//            cumulative += weights.getOrDefault(rarity, 0.0);
//            if (roll <= cumulative) {
//                return rarity;
//            }
//        }
//
//        return RarityType.common;
//    }


}
