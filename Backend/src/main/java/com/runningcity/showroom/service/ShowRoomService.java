package com.runningcity.showroom.service;

import com.runningcity.boutique.entity.Boutique;
import com.runningcity.entry.dto.EntryListResponse;
import com.runningcity.showroom.dto.UserEquippedItemRequest;
import com.runningcity.showroom.dto.UserEquippedItemResponse;
import com.runningcity.showroom.dto.UserInventoryResponse;
import com.runningcity.showroom.entity.UserEquippedItem;
import com.runningcity.showroom.entity.UserInventory;
import com.runningcity.showroom.mapper.UserEquippedItemMapper;
import com.runningcity.showroom.mapper.UserInventoryMapper;
import com.runningcity.showroom.repository.EquippedItemRepository;
import com.runningcity.showroom.repository.UserInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowRoomService {

    private final UserInventoryRepository inventoryRepository;
    private final EquippedItemRepository equippedItemRepository;
    private final UserEquippedItemMapper userEquippedItemMapper;

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
}