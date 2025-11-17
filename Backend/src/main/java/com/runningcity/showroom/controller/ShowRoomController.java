package com.runningcity.showroom.controller;

import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.showroom.dto.*;
import com.runningcity.showroom.service.PrivacySettingService;
import com.runningcity.showroom.service.ShowRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/showroom")
@RequiredArgsConstructor
public class ShowRoomController {

    private final ShowRoomService showroomService;
    private final PrivacySettingService privacySettingService;


    /**
     * 내 사무실 조회
     *
     * @param userId 사용자 ID
     * @return 쇼룸 첫화면에서 필요한 정보-사용자의 총 상태, 사용자 착장 리스트
     */
    @GetMapping("/office/{userId}")
    public ResponseEntity<ApiResponse<MyOfficeResponse>> getMyOffice(@PathVariable("userId") Long userId
    ){
//        // 현재 사용자 착장 아이템 리스트
//        List<UserEquippedItemResponse> userEquippedItemList = showroomService.getUserEquippedItemList(userId);
//        //TODO 사용자의 총 상태 조회부분 추가하기
//
//        MyOfficeResponse myOfficeResponse = MyOfficeResponse.from(userEquippedItemList);
//        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.MY_OFFICE_GET_SUCCESS,myOfficeResponse));
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.MY_OFFICE_GET_SUCCESS, showroomService.getMyOffice(userId)));
    }



    /**
     * 착장 아이템 리스트 조회
     *
     * @param userId 사용자 ID
     * @return 쇼룸 첫화면에서 필요한 정보-사용자의 총 상태, 사용자 착장 리스트
     */
    @GetMapping("/equipped/{userId}")
    public ResponseEntity<ApiResponse<List<UserEquippedItemResponse>>> getEquippedItems(@PathVariable("userId") Long userId
    ){
        // 현재 사용자 착장 아이템 리스트
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.EQUIPPED_ITEMS_GET_SUCCESS,showroomService.getUserEquippedItemList(userId)));
    }

    /**
     * 옷 갈아입히기 화면 조회 - 현재 착장은 쇼룸 첫화면에서 조회해서 프론트에서 갖고 있을 것이므로, 보유 아이템 리스트만 리턴한다.
     *
     * @param userId 사용자 ID
     * @return 사용자 보유 아이템 리스트 조회
     */
    @GetMapping("/clothes/{userId}")
    public ResponseEntity<ApiResponse<List<UserInventoryResponse>>> getClothes(@PathVariable("userId") Long userId
    ){
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.CHANGE_CLOTHES_GET_SUCCESS, showroomService.getUserInventory(userId)));
    }

    /**
     * 옷 갈아입히기 상태 저장(현재 캐릭터 착장 저장)
     *
     * @param userId 사용자 ID
     * @param reqList 사용자 착장 아이템 리스트
     * @return
     */
    @Transactional
    @PostMapping("/clothes/{userId}")
    public ResponseEntity<ApiResponse<Void>> changeClothes(@PathVariable("userId") Long userId, @RequestBody List<UserEquippedItemRequest> reqList){
//        showroomService.deleteEquippedItemAllByUserId(userId);
//        showroomService.addEquippedItem(userId,reqList);
        showroomService.changeClothes(userId,reqList);
        System.out.println(reqList);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.CHANGE_CLOTHES_POST_SUCCESS));
    }

    /**
     * 쇼룸 공개 설정 조회
     *
     * @param userId 사용자 ID
     * @return PrivacySettingResponse
     */
    @GetMapping("/privacy/{userId}")
    public ResponseEntity<ApiResponse<PrivacySettingResponse>> getPrivacySetting(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.PRIVACY_SETTING_GET_SUCCESS, privacySettingService.getPrivacySetting(userId)));
    }

    /**
     * 쇼룸 공개 설정 저장
     *
     * @param userId 사용자 ID
     * @param req PrivacySettingRequest
     * @return PrivacySettingResponse
     */
    @Transactional
    @PostMapping("/privacy/{userId}")
    public ResponseEntity<ApiResponse<Void>> savePrivacy(
            @RequestBody PrivacySettingRequest req,
            @PathVariable("userId") Long userId
    ) {
        privacySettingService.savePrivacySetting(userId, req);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.PRIVACY_SETTING_POST_SUCCESS));
    }

    // ============================================
    // ✅ 새로 추가: 글로벌 쇼룸 (랜덤 아바타 조회)
    // ============================================

    /**
     * 🎲 글로벌 쇼룸 - 랜덤 유저 아바타 조회 (친구 제외)
     *
     * 동작:
     * - 친구가 아닌 유저들을 랜덤으로 조회
     * - 각 유저의 현재 장착 아이템 포함
     *
     * @param userId 현재 로그인한 사용자 ID
     * @param size 조회할 유저 수 (기본값: 10, 최대: 50)
     * @return 랜덤 유저들의 아바타 정보
     */
    @GetMapping("/global/{userId}")
    public ResponseEntity<ApiResponse<List<RandomAvatarResponse>>> getGlobalShowroom(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 최대 조회 개수 제한
        int validSize = Math.min(size, 50);

        List<RandomAvatarResponse> randomAvatars =
                showroomService.getRandomAvatars(userId, validSize);

        return ResponseEntity.ok(
                ApiResponse.success(CommonResponseCode.SUCCESS, randomAvatars)
        );
    }

    // ============================================
    // 친구 쇼룸 보기
    // ============================================
    /**
     * 👥 친구 쇼룸 - 친구들의 아바타 조회
     *
     * 동작:
     * - 친구로 등록된 유저들의 아바타 조회
     * - 레벨 높은 순으로 정렬
     * - 각 유저의 현재 장착 아이템 포함
     *
     * @param userId 현재 로그인한 사용자 ID
     * @param size 조회할 친구 수 (선택, 기본값: 전체, 최대: 50)
     * @return 친구들의 아바타 정보
     */
    @GetMapping("/friends/{userId}")
    public ResponseEntity<ApiResponse<List<RandomAvatarResponse>>> getFriendShowroom(
            @PathVariable("userId") Long userId,
            @RequestParam(required = false) Integer size
    ) {
        List<RandomAvatarResponse> friendAvatars =
                showroomService.getFriendAvatars(userId, size);

        return ResponseEntity.ok(
                ApiResponse.success(CommonResponseCode.SUCCESS, friendAvatars)
        );
    }


}
