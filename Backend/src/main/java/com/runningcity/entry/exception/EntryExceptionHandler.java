package com.runningcity.entry.exception;

import com.runningcity.global.exception.BaseException;
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
@RestControllerAdvice(basePackages = "com.runningcity.entry")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EntryExceptionHandler {

    /**
     * 잠입 기지 요청 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleEntryValidationException(MethodArgumentNotValidException e) {
        log.error("Entry Validation Exception: {}", e.getMessage());

        // 모든 필드 에러를 ErrorDetail로 변환
        List<ErrorDetail> errorDetails = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorDetails.add(new ErrorDetail(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            ));
        }

        CommonResponseCode responseCode = CommonResponseCode.ENTRY_BAD_REQUEST;
        ApiResponse<Void> response = ApiResponse.fail(responseCode, errorDetails);

        return ResponseEntity.status(responseCode.getHttpStatus()).body(response);
    }

}