package com.runningcity.boutique.controller;

import com.runningcity.boutique.dto.*;
import com.runningcity.boutique.exception.BoutiqueResponseCode;
import com.runningcity.boutique.service.BoutiqueService;
import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boutique")
@RequiredArgsConstructor
public class BoutiqueController {

    private final BoutiqueService boutiqueService;

    //스토어 에서 구매할 수 있는 목록 불러오기
    @GetMapping("/store/{userId}")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getStoreItem(@PathVariable Long userId
    ){
        List<StoreResponse> storeList = boutiqueService.getStoreItems(userId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS,storeList));
    }

    /**
     * 상점 아이템 구매
     */
    @PostMapping("/store/{userId}")
    public ResponseEntity<ApiResponse<PurchaseResponse>> purchaseItem(
            @PathVariable Long userId,
            @Valid @RequestBody PurchaseRequest request
    ) {
        PurchaseResponse response = boutiqueService.purchaseItem(userId,request);
        return ResponseEntity.ok(
                ApiResponse.success(BoutiqueResponseCode.ITEM_BUY_SCCESS, response)
        );
    }


    /**
     * 가챠 뽑기
     *
     * @param userId 사용자 ID
     * @param request 가챠 요청 (single or multi)
     * @return 뽑은 아이템 목록
     */
    @PostMapping("/gacha/{userId}")
    public ResponseEntity<ApiResponse<GachaResponse>> drawGacha(
            @PathVariable Long userId,
            @Valid @RequestBody GachaRequest request
    ) {
        GachaResponse response = boutiqueService.drawGacha(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success(BoutiqueResponseCode.GACHA_SUCCESS, response)
        );
    }


}
