package com.runningcity.showroom.controller;

import com.runningcity.boutique.dto.StoreResponse;
import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.showroom.dto.UserEquippedItemResponse;
import com.runningcity.showroom.service.ShowRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/showroom/me")
@RequiredArgsConstructor
public class MyShowroomController {

    private final ShowRoomService showroomService;

    // 내 사무실 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<UserEquippedItemResponse>>> getMyShowroom(@PathVariable Long userId
    ){
        List<UserEquippedItemResponse> userEquippedItemList = showroomService.getUserEquippedItemList(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS,userEquippedItemList));
    }



}
