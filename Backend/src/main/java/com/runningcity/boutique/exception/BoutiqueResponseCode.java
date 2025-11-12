package com.runningcity.boutique.exception;

import com.runningcity.global.response.BaseResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BoutiqueResponseCode implements BaseResponseCode {

    //성공
    ITEM_BUY_SCCESS  (HttpStatus.OK , "BOUTIQUE_000","상품 구매가 성공적으로 완료되었습니다."),

    // 4xx (클라이언트 오류)
    ITEM_NOT_FOUND           (HttpStatus.NOT_FOUND,   "BOUTIQUE_001", "상품을 찾을 수 없습니다."),
    ALREADY_PURCHASED        (HttpStatus.CONFLICT,    "BOUTIQUE_002", "이미 구매한 상품입니다."),
    INSUFFICIENT_CREDITS     (HttpStatus.BAD_REQUEST, "BOUTIQUE_003", "크레딧이 부족합니다."),
    NOT_STORE_ITEM           (HttpStatus.BAD_REQUEST, "BOUTIQUE_004", "상점에서 구매할 수 없는 상품입니다."),

    // 5xx (서버 오류)
    PURCHASE_FAILED          (HttpStatus.INTERNAL_SERVER_ERROR, "BOUTIQUE_500", "구매 처리 중 오류가 발생했습니다.");
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    BoutiqueResponseCode(HttpStatus status, String code, String message) {
        this.httpStatus = status;
        this.code = code;
        this.message = message;
    }




}
