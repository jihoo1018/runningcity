package com.runningcity.global.exception;

import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.BaseResponseCode;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.global.response.ErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
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
        BaseResponseCode code = e.getResponseCode();

        if (code.getHttpStatus().is4xxClientError()) {
            log.warn("BaseException code={}, msg={}", code.getCode(), e.getMessage());
        } else {
            log.error("BaseException code={}, msg={}", code.getCode(), e.getMessage());
        }

        // 변경: ApiResponse.error(ErrorCode errorCode) 호출
        ApiResponse<Void> response = ApiResponse.fail(code);

        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }

    /**
     * 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorDetail(fe.getField(), fe.getDefaultMessage()))
                .toList();

        CommonResponseCode code = CommonResponseCode.INVALID_INPUT_VALUE;
        log.warn("Validation failed: {}", details);

        ApiResponse<Void> response = ApiResponse.fail(code, details);
        return ResponseEntity.status(code.getHttpStatus()).body(response);
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