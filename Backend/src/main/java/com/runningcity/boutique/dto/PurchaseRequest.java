package com.runningcity.boutique.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상점 구매 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {
    
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long itemId;
}