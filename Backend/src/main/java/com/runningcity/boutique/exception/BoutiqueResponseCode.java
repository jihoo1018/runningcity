package com.runningcity.boutique.exception;

import com.runningcity.global.response.BaseResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BoutiqueResponseCode implements BaseResponseCode {
    // 4xx (클라이언트 오류)
    SESSION_NOT_FOUND        (HttpStatus.NOT_FOUND,           "RUN_4040", "세션을 찾을 수 없습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    BoutiqueResponseCode(HttpStatus status, String code, String message) {
        this.httpStatus = status;
        this.code = code;
        this.message = message;
    }




}
