package com.runningcity.global.exception;

import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * BaseException 처리
     */
    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        log.error("BaseException: {}", e.getMessage());
        CommonResponseCode commonResponseCode = e.getCommonResponseCode();

        // 변경: ApiResponse.error(ErrorCode errorCode) 호출
        ApiResponse<Void> response = ApiResponse.fail(commonResponseCode);

        return ResponseEntity.status(commonResponseCode.getHttpStatus()).body(response);
    }

    /**
     * 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());

        String errorMessage = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "입력값 검증에 실패했습니다.";

        CommonResponseCode commonResponseCode = CommonResponseCode.INVALID_INPUT_VALUE; // INVALID_INPUT_VALUE 사용
        ApiResponse<Void> response = ApiResponse.fail(commonResponseCode);

        return ResponseEntity.status(commonResponseCode.getHttpStatus()).body(response);
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception: {}", e.getMessage(), e);

        CommonResponseCode commonResponseCode = CommonResponseCode.INTERNAL_SERVER_ERROR;
        ApiResponse<Void> response = ApiResponse.fail(commonResponseCode);

        return ResponseEntity.status(commonResponseCode.getHttpStatus()).body(response);
    }
}