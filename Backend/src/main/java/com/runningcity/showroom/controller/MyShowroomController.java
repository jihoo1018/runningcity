package com.runningcity.showroom.controller;

import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.showroom.dto.PrivacySettingRequest;
import com.runningcity.showroom.dto.PrivacySettingResponse;
import com.runningcity.showroom.dto.UserEquippedItemResponse;
import com.runningcity.showroom.dto.UserInventoryResponse;
import com.runningcity.showroom.service.PrivacySettingService;
import com.runningcity.showroom.service.ShowRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/showroom")
@RequiredArgsConstructor
public class MyShowroomController {

    private final ShowRoomService showroomService;
    private final PrivacySettingService privacySettingService;


    /**
     * 내 사무실 조회
     *
     * @param userId 사용자 ID
     * @return 쇼룸 첫화면에서 필요한 정보-사용자의 총 상태, 사용자 착장 리스트
     */
    @GetMapping("/office/{userId}")
    public ResponseEntity<ApiResponse<List<UserEquippedItemResponse>>> getMyShowroom(@PathVariable("userId") Long userId
    ){
        //TODO 사용자의 총 상태 조회부분 추가하기
        
        // 현재 사용자 착장 아이템 리스트
        List<UserEquippedItemResponse> userEquippedItemList = showroomService.getUserEquippedItemList(userId); 
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.MY_OFFICE_GET_SUCCESS,userEquippedItemList));
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
     * @return
     */
    @PostMapping("/clothes/{userId}")
    public ResponseEntity<ApiResponse<Void>> changeClothes(@PathVariable("userId") Long userId
    ){
        // TODO
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
    @PostMapping("/privacy/{userId}")
    public ResponseEntity<ApiResponse<Void>> savePrivacy(
            @RequestBody PrivacySettingRequest req,
            @PathVariable("userId") Long userId
    ) {
        privacySettingService.savePrivacySetting(userId, req);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.PRIVACY_SETTING_POST_SUCCESS));
    }
}
