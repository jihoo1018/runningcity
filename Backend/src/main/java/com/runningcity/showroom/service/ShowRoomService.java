package com.runningcity.showroom.service;

import com.runningcity.boutique.entity.Boutique;
import com.runningcity.boutique.repository.BoutiqueRepository;
import com.runningcity.entry.dto.EntryListResponse;
import com.runningcity.friendship.repository.FriendshipRepository;
import com.runningcity.showroom.dto.RandomAvatarResponse;
import com.runningcity.showroom.dto.UserEquippedItemRequest;
import com.runningcity.showroom.dto.UserEquippedItemResponse;
import com.runningcity.showroom.dto.UserInventoryResponse;
import com.runningcity.showroom.entity.UserEquippedItem;
import com.runningcity.showroom.entity.UserInventory;
import com.runningcity.showroom.mapper.UserEquippedItemMapper;
import com.runningcity.showroom.mapper.UserInventoryMapper;
import com.runningcity.showroom.repository.EquippedItemRepository;
import com.runningcity.showroom.repository.UserInventoryRepository;
import com.runningcity.user.entity.User;
import com.runningcity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowRoomService {

    private final UserInventoryRepository inventoryRepository;
    private final EquippedItemRepository equippedItemRepository;
    private final UserEquippedItemMapper userEquippedItemMapper;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final BoutiqueRepository boutiqueRepository;

    /**
     * 인벤토리에 신규 아이템 추가
     * (상점 구매 전용 - 중복 구매는 이미 Controller/Service에서 검증됨)
     *
     * @param userId 유저 ID
     * @param itemId 아이템 ID
     * @return 생성된 인벤토리 ID
     */
    @Transactional(propagation = Propagation.MANDATORY) //부모 트랜젝션이 존재하고 그 하위에서 굴러가는 트랜젝션에 붙이는 메서드
    public Long addItem(Long userId, Long itemId) {
        // 신규 아이템 생성 (quantity = 1 고정)
        UserInventory newInventory = UserInventory.create(userId, itemId);
        UserInventory saved = inventoryRepository.save(newInventory);
        return saved.getInventoryId();
    }

    // ============================================
    // 2️⃣ 가챠 전용 메서드
    // ============================================

    /**
     * 가챠 획득 - 신규 추가 또는 수량 증가
     *
     * 특징:
     * - 중복 획득 가능
     * - 중복 시 quantity += 1
     * - 같은 아이템 여러 번 뽑을 수 있음
     *
     * @param userId 사용자 ID
     * @param itemId 아이템 ID
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void addItemFromGacha(Long userId, Long itemId) {
        // 이미 보유한 아이템인지 확인
        Optional<UserInventory> existing = inventoryRepository
                .findByUserIdAndItemId(userId, itemId);

        if (existing.isPresent()) {
            // 중복 획득 → 수량 +1
            UserInventory inventory = existing.get();
            inventory.increaseQuantity(1);

            log.info("🎰 [가챠 중복] userId={}, itemId={}, 수량: {} → {}",
                    userId, itemId, inventory.getQuantity() - 1, inventory.getQuantity());
        } else {
            // 첫 획득 → 신규 추가
            UserInventory newInventory = UserInventory.create(userId, itemId);
            inventoryRepository.save(newInventory);

            log.info("🎰 [가챠 신규] userId={}, itemId={}", userId, itemId);
        }
    }

    /**
     * 유저가 특정 아이템을 보유하고 있는지 확인
     * 
     * @param userId 유저 ID
     * @param itemId 아이템 ID
     * @return 보유 여부
     */
    @Transactional(readOnly = true)
    public boolean hasItem(Long userId, Long itemId) {
        return inventoryRepository.existsByUserIdAndItemId(userId, itemId);
    }


    /**
     * 유저가 현재 착용한 아이템 리스트 조회
     *
     * @param userId 유저 ID
     * @return 유저가 현재 착용한 아이템 리스트
     */
    public List<UserEquippedItemResponse> getUserEquippedItemList(Long userId) {

//        return equippedItemRepository.findByUserId(userId)
//                .stream()
//                .map(UserEquippedItemResponse::fromEntity)
//                .collect(Collectors.toList());
        return equippedItemRepository.findEquippedItemsByUserId(userId);
    }


    /**
     * 유저가 갖고있는 아이템 리스트 조회
     *
     * @param userId 유저 ID
     * @return 유저가 갖고있는 아이템 리스트
     */
    public List<UserInventoryResponse> getUserInventory(Long userId) {

        List<Object[]> rows = inventoryRepository.findInventoryJoinItem(userId);

        return rows.stream()
                .map(row -> {
                    UserInventory ui = (UserInventory) row[0];
                    Boutique b = (Boutique) row[1];
                    return UserInventoryMapper.toDto(ui, b);
                })
                .toList();
    }
//    public List<UserInventoryResponse> getUserInventoryList(Long userId) {
//
//        return inventoryRepository.findByUserId(userId)
//                .stream()
//                .map(UserInventoryResponse::fromEntity)
//                .collect(Collectors.toList());
//    }

    @Transactional
    public void changeClothes(Long userId, List<UserEquippedItemRequest> reqList){
        this.deleteEquippedItemAllByUserId(userId);
        this.addEquippedItem(userId, reqList);
    }

    // 유저 착장 리스트 저장
    @Transactional(propagation = Propagation.MANDATORY)
    public void addEquippedItem(Long userId, List<UserEquippedItemRequest> reqList) {
        for (UserEquippedItemRequest req : reqList) {
            if (req.getItemId()!= null && req.getItemId() != 0) {
                UserEquippedItem equippedItem = userEquippedItemMapper.toEntity(userId, req);
                log.info(equippedItem.toString());
                equippedItemRepository.save(equippedItem);
            }
        }
    }

    // 유저 착장 리스트 전체 삭제
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteEquippedItemAllByUserId(Long userId) {
        equippedItemRepository.deleteAllByUserId(userId);
    }

    /**
     랜덤 유저 아바타 조회 (친구 제외)
     *
     * 동작 흐름:
     * 1. 친구 목록 조회
     * 2. 친구 + 본인 제외한 랜덤 유저 조회
     * 3. 아이템 정보 캐싱 조회
     * 4. 장착 정보 일괄 조회 (N+1 방지)
     * 5. 응답 DTO 조립
     *
     * @param currentUserId 현재 로그인한 유저 ID
     * @param size 조회할 유저 수 (최대 50)
     * @return 랜덤 유저들의 아바타 정보
     */
    @Transactional(readOnly = true)
    public List<RandomAvatarResponse> getRandomAvatars(Long currentUserId, int size) {
        log.info("🎲 [시작] 랜덤 아바타 조회 - userId: {}, size: {}", currentUserId, size);

        // ✅ Step 1: 친구 목록 조회
        List<Long> friendUserIds = friendshipRepository.findAllRelatedUserIds(currentUserId);
        log.debug("👥 친구 목록: {} ({}명)", friendUserIds, friendUserIds.size());

        // ✅ Step 2: 제외할 유저 목록 (본인 + 친구들)
        List<Long> excludedUserIds = new ArrayList<>(friendUserIds);
        excludedUserIds.add(currentUserId);
        log.debug("🚫 제외 목록: {} ({}명)", excludedUserIds, excludedUserIds.size());

        // ✅ Step 3: 랜덤 유저 조회
        Pageable pageable = PageRequest.of(0, size);
        List<User> randomUsers = userRepository.findRandomNonFriends(
                excludedUserIds,
                pageable
        );

        if (randomUsers.isEmpty()) {
            log.warn("⚠️ 조회 가능한 유저가 없습니다.");
            return List.of();
        }

        log.info("✅ 조회된 랜덤 유저 수: {}", randomUsers.size());

        // ✅ Step 4: 아이템 정보 조회 (캐싱!)
        Map<Long, Boutique> itemMap = getAllItemsMap();
        log.debug("📦 아이템 맵 로드: {}개", itemMap.size());

        // ✅ Step 5: 장착 정보 일괄 조회 (N+1 방지!)
        List<Long> userIds = randomUsers.stream()
                .map(User::getUserId)
                .toList();

        List<UserEquippedItem> allEquippedItems =
                equippedItemRepository.findAllByUserIdIn(userIds);

        log.debug("🎨 장착 아이템 조회: {}개", allEquippedItems.size());

        // ✅ Step 6: userId별로 그룹핑
        Map<Long, List<UserEquippedItem>> equippedByUser = allEquippedItems.stream()
                .collect(Collectors.groupingBy(UserEquippedItem::getUserId));

        // ✅ Step 7: 응답 DTO 생성
        List<RandomAvatarResponse> result = randomUsers.stream()
                .map(user -> buildRandomAvatarResponse(
                        user,
                        equippedByUser.get(user.getUserId()),
                        itemMap
                ))
                .toList();

        log.info("✅ [완료] 랜덤 아바타 조회 성공 - 반환: {}명", result.size());
        return result;
    }

    /**
     * 📦 모든 아이템 정보 조회 (캐싱!)
     *
     * 캐시 동작:
     * - 최초 1회만 DB 조회
     * - 이후 Redis에서 조회
     * - 서버 재시작 전까지 유지
     *
     * @return itemId를 키로 하는 아이템 맵
     */
    @Cacheable(value = "allItems")
    public Map<Long, Boutique> getAllItemsMap() {
        log.info("🔍 [캐시 미스] 아이템 정보 DB 조회 시작");

        List<Boutique> allItems = boutiqueRepository.findAll();

        Map<Long, Boutique> result = allItems.stream()
                .collect(Collectors.toMap(Boutique::getItemId, item -> item));

        log.info("✅ [캐시 저장] 아이템 {}개 로드 완료", result.size());
        return result;
    }

    /**
     * 🎨 User 엔티티 → RandomAvatarResponse 변환
     *
     * @param user 유저 엔티티
     * @param equippedItems 해당 유저의 장착 아이템 목록 (null 가능)
     * @param itemMap 전체 아이템 맵 (캐싱된 데이터)
     * @return RandomAvatarResponse DTO
     */
    private RandomAvatarResponse buildRandomAvatarResponse(
            User user,
            List<UserEquippedItem> equippedItems,
            Map<Long, Boutique> itemMap
    ) {
        // 장착 아이템 DTO 변환
        List<RandomAvatarResponse.EquippedItemDto> items =
                (equippedItems != null)
                        ? equippedItems.stream()
                        .map(equipped -> {
                            Boutique item = itemMap.get(equipped.getItemId());

                            // 아이템 정보가 없는 경우 (데이터 정합성 문제)
                            if (item == null) {
                                log.warn("⚠️ 아이템 정보 없음: userId={}, itemId={}",
                                        user.getUserId(), equipped.getItemId());
                                return null;
                            }

                            return RandomAvatarResponse.EquippedItemDto.builder()
                                    .itemId(item.getItemId())
                                    .category(item.getCategory())
                                    .subcategory(item.getSubcategory())
                                    .style(item.getStyle())
                                    .basePath(item.getBasePath())
                                    .build();
                        })
                        .toList()
                        : List.of();  // equippedItems가 null이면 빈 리스트

        return RandomAvatarResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .level(user.getLevel())
                .equippedItems(items)
                .build();
    }

}