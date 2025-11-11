package com.runningcity.boutique.controller;

import com.runningcity.boutique.dto.StoreResponse;
import com.runningcity.boutique.service.BoutiqueService;
import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/boutique")
@RequiredArgsConstructor
public class BoutiqueController {

    private static final long userId = 1L; // 로그인 없으니 임시 1 고정
    private final BoutiqueService boutiqueService;

    //스토어 에서 구매할 수 있는 목록 불러오기
    @GetMapping("/store") 
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getStoreItem(
            // @AuthenticationPrincipal Long userId
    ){
        List<StoreResponse> storeList = boutiqueService.getStoreItems(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS,storeList));
    }



}
