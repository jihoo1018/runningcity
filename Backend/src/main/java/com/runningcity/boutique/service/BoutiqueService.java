package com.runningcity.boutique.service;

import com.runningcity.boutique.dto.*;
import com.runningcity.boutique.entity.BoutiqueRarityRate;
import com.runningcity.boutique.entity.GachaHistory;
import com.runningcity.boutique.enums.DrawType;
import com.runningcity.boutique.enums.ObtainMethod;
import com.runningcity.boutique.enums.RarityType;
import com.runningcity.boutique.exception.BoutiqueResponseCode;
import com.runningcity.boutique.repository.BoutiqueRarityRateRepository;
import com.runningcity.boutique.repository.GachaHistoryRepository;
import com.runningcity.global.exception.BaseException;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.showroom.repository.UserInventoryRepository;
import com.runningcity.boutique.entity.Boutique;
import com.runningcity.boutique.repository.BoutiqueRepository;
import com.runningcity.showroom.service.ShowRoomService;
import com.runningcity.user.entity.User;
import com.runningcity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoutiqueService {

    private final BoutiqueRepository boutiqueRepository;
    private final UserInventoryRepository showRoomRepository;
    private final UserRepository userRepository;
    private final ShowRoomService showRoomService;
    private final GachaHistoryRepository gachaHistoryRepository;

    //가챠용
    private static final long SINGLE_GACHA_PRICE = 50L;
    private static final long MULTI_GACHA_PRICE = 450L;

    /**
     * 🎲 난수 생성기 (싱글톤)
     * - SecureRandom은 생성 비용이 크므로 재사용
     * - ThreadSafe하므로 여러 요청에서 동시 사용 가능
     */
    private final SecureRandom random = new SecureRandom();
    private final BoutiqueRarityRateRepository boutiqueRarityRateRepository;

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
     * 🎲 등급별 확률 조회 (Redis 캐싱)
     *
     * @return Map<등급, 확률>
     *
     * 동작:
     * - boutique_rarity_rates 테이블에서 조회
     * - Redis에 1시간 캐싱
     *
     * 캐시 키: "gachaRates"
     */
    @Cacheable(value = "gachaRates")
    public Map<RarityType, Double> getGachaRates() {
        log.info("🔍 [캐시 미스] DB에서 가챠 확률 조회 중...");

        List<BoutiqueRarityRate> rates = boutiqueRarityRateRepository.findAll();


        // Map으로 변환
        Map<RarityType, Double> weights = new HashMap<>();

        for (BoutiqueRarityRate rate : rates) {
            RarityType rarityType = rate.getRarity();
            // BigDecimal → double 변환
            double probability = rate.getProbability().doubleValue() / 100.0;
            weights.put(rarityType, probability);

            log.debug("📊 {}등급 확률: {}%", rarityType, rate.getProbability());
        }

        log.info("✅ [캐시 저장] 등급별 확률 로드 완료: {}개", weights.size());

        return weights;
    }

    /**
     * 📦 등급별 가챠 아이템 풀 조회 (Redis 캐싱)
     *
     * 동작:
     * - boutique_items 테이블에서 obtainMethod='gacha'인 아이템 조회
     * - 등급별(COMMON, RARE, EPIC, LEGENDARY)로 그룹화
     * - Redis에 1시간 캐싱
     *
     * @return Map<등급, 해당 등급 아이템 리스트>
     */
    @Cacheable(value = "gachaItemsByRarity")
    public Map<RarityType, List<Boutique>> getGachaItemsByRarity() {
        log.info("🔍 [캐시 미스] DB에서 가챠 아이템 풀 조회 중...");
        // 1️⃣ DB에서 가챠 전용 아이템 조회
        List<Boutique> gachaPool = boutiqueRepository.findByObtainMethod(ObtainMethod.gacha);
        if (gachaPool.isEmpty()) {
            throw new BaseException(BoutiqueResponseCode.NO_GACHA_ITEMS);
        }
        // 2️⃣ 등급별로 그룹화
        Map<RarityType, List<Boutique>> itemsByRarity = new HashMap<>();
        for (Boutique item : gachaPool) {
            RarityType rarity = item.getRarity();
            // 해당 등급 리스트가 없으면 생성 후 아이템 추가
            itemsByRarity.computeIfAbsent(rarity, k -> new ArrayList<>()).add(item);
        }
        // 3️⃣ 로그 출력
        log.info("✅ [캐시 저장] 등급별 아이템 수:");
        log.info("   - COMMON: {}개", itemsByRarity.getOrDefault(RarityType.common, Collections.emptyList()).size());
        log.info("   - RARE: {}개", itemsByRarity.getOrDefault(RarityType.rare, Collections.emptyList()).size());
        log.info("   - EPIC: {}개", itemsByRarity.getOrDefault(RarityType.epic, Collections.emptyList()).size());
        log.info("   - LEGENDARY: {}개", itemsByRarity.getOrDefault(RarityType.legendary, Collections.emptyList()).size());
        return itemsByRarity;
    }

    /**
     * 🎲 확률 기반 등급 선택
     *
     * 동작:
     * - 0.0~1.0 사이 랜덤 숫자 생성
     * - 누적 확률로 등급 결정
     *
     * 예시:
     * - roll = 0.30 → COMMON (0~0.60)
     * - roll = 0.75 → RARE (0.60~0.85)
     * - roll = 0.98 → LEGENDARY (0.95~1.00)
     *
     * @param rarityWeights DB에서 조회한 확률 맵
     * @return 선택된 등급
     */
    private RarityType selectRarityByProbability(Map<RarityType, Double> rarityWeights) {
        // 0.0 ~ 1.0 사이 랜덤 숫자
        double roll = random.nextDouble();

        double cumulative = 0.0;  // 누적 확률

        // 등급 순서 보장 (COMMON → RARE → EPIC → LEGENDARY)
        List<RarityType> orderedRarities = Arrays.asList(
                RarityType.common,
                RarityType.rare,
                RarityType.epic,
                RarityType.legendary
        );

        // 누적 확률로 등급 결정
        for (RarityType rarity : orderedRarities) {
            cumulative += rarityWeights.getOrDefault(rarity, 0.0);

            // roll이 누적 확률보다 작으면 이 등급!
            if (roll < cumulative) {
                log.debug("🎲 [등급 결정] roll={}, 선택={}", String.format("%.4f", roll), rarity);
                return rarity;
            }
        }

        // 폴백 (여기 도달하면 안됨)
        log.warn("⚠️ [확률 오류] 기본값 COMMON 반환");
        return RarityType.common;
    }

    /**
     * 🎲 여러 아이템 랜덤 뽑기
     *
     * 프로세스:
     * 1. count만큼 반복 (1회 또는 10회)
     * 2. 각 회차마다:
     *    - 등급 결정 (확률 기반)
     *    - 해당 등급 내에서 랜덤 아이템 선택
     *
     * @param itemsByRarity 등급별 아이템 맵 (Redis에서 조회한 데이터)
     * @param rarityWeights 등급별 확률 맵 (DB에서 조회한 데이터)
     * @param count 뽑을 개수 (1 or 10)
     * @return 뽑힌 아이템 리스트
     */
    private List<Boutique> drawRandomItems(
            Map<RarityType, List<Boutique>> itemsByRarity,
            Map<RarityType, Double> rarityWeights,
            int count) {

        List<Boutique> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Step 1: 등급 결정 (확률 기반)
            RarityType selectedRarity = selectRarityByProbability(rarityWeights);
            // Step 2: 해당 등급의 아이템 목록 가져오기
            List<Boutique> candidates = itemsByRarity.get(selectedRarity);
            // Step 3: 예외 처리 (아이템 없는 경우)
            if (candidates == null || candidates.isEmpty()) {
                log.warn("⚠️ [가챠 경고] {}등급 아이템이 없습니다", selectedRarity);

                // 차선책: 모든 등급 통합해서 랜덤 선택
                List<Boutique> allItems = itemsByRarity.values().stream()
                        .flatMap(List::stream)
                        .toList();

                if (!allItems.isEmpty()) {
                    result.add(allItems.get(random.nextInt(allItems.size())));
                }
                continue;
            }
            // Step 4: 해당 등급 내에서 랜덤 선택
            int randomIndex = random.nextInt(candidates.size());
            Boutique selectedItem = candidates.get(randomIndex);

            result.add(selectedItem);

            log.debug("🎰 [{}회차] {}등급 → {} 획득",
                    i + 1, selectedRarity, selectedItem.getName());
        }

        return result;
    }

    /**
     * 🎰 가챠 뽑기 메인 메서드
     *
     * 전체 흐름:
     * 1. 사용자 크레딧 확인 및 차감
     * 2. Redis에서 아이템 목록 조회
     * 3. 확률 기반 랜덤 뽑기
     * 4. DB에 이력 저장
     * 5. 인벤토리 추가
     *
     * @param userId 사용자 ID
     * @param request 가챠 요청 (single/multi)
     * @return 뽑은 아이템 정보
     */
    @Transactional
    public GachaResponse drawGacha(Long userId, GachaRequest request) {
        DrawType drawType = request.getDrawType();

        // 1️⃣ 가격 및 뽑기 개수 결정
        long price = getGachaPrice(drawType);
        int quantity = getGachaQuantity(drawType);

        log.info("🎰 [가챠 시작] userId={}, drawType={}, quantity={}, price={}",
                userId, drawType, quantity, price);

        // 2️⃣ 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.USER_NOT_FOUND));

        // 3️⃣ 크레딧 확인 및 차감
        if (user.getTotalCredit() < price) {
            throw new BaseException(BoutiqueResponseCode.INSUFFICIENT_CREDITS);
        }
        user.deductCredit((int) price);

        log.debug("💰 [크레딧 차감] 차감: {}, 잔액: {}", price, user.getTotalCredit());

        // 4️⃣ 등급별 아이템 풀 조회 (Redis 캐싱!)
        Map<RarityType, List<Boutique>> itemsByRarity = getGachaItemsByRarity();

        // 5️⃣ 확률 정보 조회 (Redis 캐싱!)
        Map<RarityType, Double> rarityWeights = getGachaRates();

        // 6️⃣ 랜덤 뽑기 실행
        UUID sessionId = UUID.randomUUID();
        List<Boutique> drawnItems = drawRandomItems(itemsByRarity, rarityWeights, quantity);

        log.info("🎁 [뽑기 결과] 총 {}개 아이템", drawnItems.size());

        // 7️⃣ 가챠 이력 저장
        List<GachaHistory> histories = drawnItems.stream()
                .map(item -> GachaHistory.create(
                        userId,
                        item.getItemId(),
                        item.getRarity(),
                        drawType,
                        sessionId
                ))
                .toList();
        gachaHistoryRepository.saveAll(histories);

        log.debug("📝 [이력 저장] {}개 기록 저장 완료", histories.size());

        // 8️⃣ 인벤토리에 추가
        List<GachaResponse.GachaItem> responseItems = new ArrayList<>();

        for (Boutique item : drawnItems) {
            boolean isNew = !showRoomService.hasItem(userId, item.getItemId());
            showRoomService.addItemFromGacha(userId, item.getItemId());

            responseItems.add(GachaResponse.GachaItem.builder()
                    .itemId(item.getItemId())
                    .itemName(item.getName())
                    .category(item.getCategory())
                    .subcategory(item.getSubcategory())
                    .style(item.getStyle())
                    .rarity(item.getRarity())
                    .path(item.getBasePath())
                    .isNew(isNew)
                    .build());

            log.debug("🎁 [획득] {} ({}등급) {}",
                    item.getName(), item.getRarity(), isNew ? "✨신규" : "중복");
        }

        // 9️⃣ 응답 생성
        log.info("✅ [가챠 완료] sessionId={}, items={}", sessionId, drawnItems.size());

        return GachaResponse.builder()
                .sessionId(sessionId)
                .drawType(drawType)
                .totalDraws(quantity)
                .spentCredit(price)
                .remainingCredit(user.getTotalCredit())
                .items(responseItems)
                .build();
    }
    /**
     * 💰 가챠 타입별 가격 반환
     */
    private long getGachaPrice(DrawType drawType) {
        return switch (drawType) {
            case single -> SINGLE_GACHA_PRICE;   // 50
            case multi -> MULTI_GACHA_PRICE;     // 450
        };
    }

    /**
     * 🎰 가챠 타입별 뽑기 개수 반환
     */
    private int getGachaQuantity(DrawType drawType) {
        return switch (drawType) {
            case single -> 1;
            case multi -> 10;
        };
    }
}
