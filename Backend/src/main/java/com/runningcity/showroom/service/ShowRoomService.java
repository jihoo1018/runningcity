package com.runningcity.showroom.service;

import com.runningcity.showroom.entity.UserInventory;
import com.runningcity.showroom.repository.ShowRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowRoomService {

    private final ShowRoomRepository showRoomRepository;

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
        UserInventory saved = showRoomRepository.save(newInventory);
        return saved.getInventoryId();
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
        return showRoomRepository.existsByUserIdAndItemId(userId, itemId);
    }

}