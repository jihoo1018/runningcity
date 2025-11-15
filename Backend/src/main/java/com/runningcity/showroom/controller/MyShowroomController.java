package com.runningcity.showroom.controller;

import com.runningcity.boutique.dto.StoreResponse;
import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.showroom.dto.PrivacySettingRequest;
import com.runningcity.showroom.dto.PrivacySettingResponse;
import com.runningcity.showroom.dto.UserEquippedItemResponse;
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
    private final PrivacySettingService service;


    /**
     * 내 사무실 조회
     *
     * @param userId 사용자 ID
     * @return 
     */
    @GetMapping("/me/{userId}")
    public ResponseEntity<ApiResponse<List<UserEquippedItemResponse>>> getMyShowroom(@PathVariable("userId") Long userId
    ){
        //TODO 사용자의 총 상태 조회부분 추가하기
        List<UserEquippedItemResponse> userEquippedItemList = showroomService.getUserEquippedItemList(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS,userEquippedItemList));
    }

    /**
     * 옷 갈아입히기 화면 조회
     *
     * @param userId 사용자 ID
     * @return
     */
    @GetMapping("/clothes/{userId}")
    public ResponseEntity<ApiResponse<List<UserEquippedItemResponse>>> getClothesRoom(@PathVariable("userId") Long userId
    ){
        // TODO
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS,null));
    }

    /**
     * 옷 갈아입히기 상태 저장(현재 캐릭터 착장 저장)
     *
     * @param userId 사용자 ID
     * @return
     */
    @GetMapping("/clothes/{userId}")
    public ResponseEntity<ApiResponse<List<UserEquippedItemResponse>>> changeClothes(@PathVariable("userId") Long userId
    ){
        // TODO
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS,null));
    }


    /**
     * 쇼룸 공개 설정 저장
     *
     * @param
     * @return
     */
    @PostMapping("/privacy")
    public ResponseEntity<ApiResponse<PrivacySettingResponse>> savePrivacy(
            @RequestBody PrivacySettingRequest req,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        // TODO
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS,null));
    }
}
