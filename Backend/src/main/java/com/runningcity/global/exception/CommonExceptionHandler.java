package com.runningcity.global.exception;

import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.BaseResponseCode;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.global.response.ErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * 잠입 기지 도메인 전용 예외 처리 핸들러
 * CommonExceptionHandler보다 우선순위가 높아 온보딩 관련 예외를 먼저 처리합니다.
 */
@Slf4j
@RestControllerAdvice   //(basePackages = "com.runningcity.entry")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonExceptionHandler {

    /** 알 수 없는 예외 처리 (catch-all) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        e.printStackTrace(); // 로그
        return ResponseEntity
                .status(CommonResponseCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.fail(CommonResponseCode.INTERNAL_SERVER_ERROR));
    }


    /** 커스텀 도메인 예외 처리 */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        BaseResponseCode code = e.getResponseCode();
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ApiResponse.fail(code));
    }

    /** 유효성 검증 실패 (ex. @Valid) */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {

        var details = e.getBindingResult().getFieldErrors().stream()
                .map(field -> new ErrorDetail(field.getField(), field.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .status(CommonResponseCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(ApiResponse.fail(CommonResponseCode.INVALID_INPUT_VALUE, details));
    }

}