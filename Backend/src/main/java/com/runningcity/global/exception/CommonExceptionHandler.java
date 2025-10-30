package com.runningcity.global.exception;

import com.A606.hon_moon.global.common.dto.ApiResponse_legacy;
import com.A606.hon_moon.global.common.enums.ErrorCode;
import com.runningcity.global.common.ApiResponse;
import com.runningcity.global.common.CommonResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
// import javax.naming.AuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * BaseException 처리
     */
    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException e) {
        log.error("BaseException: {}", e.getMessage());
        CommonResponseCode commonResponseCode = e.getCommonResponseCode();

        // 변경: ApiResponse.error(ErrorCode errorCode) 호출
        ApiResponse<Object> response = ApiResponse.error(commonResponseCode);

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
        // 변경: ApiResponse.error(ErrorCode errorCode) 호출
        ApiResponse<Void> response = ApiResponse.fail(commonResponseCode);

        return ResponseEntity.status(commonResponseCode.getHttpStatus()).body(response);
    }

    /**
     *  바인딩 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ApiResponse_legacy<Object>> handleBindException(BindException e) {
        log.error("BindException: {}", e.getMessage());

        String errorMessage = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "입력값 바인딩에 실패했습니다.";

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE; // INVALID_INPUT_VALUE 사용
        // 변경: ApiResponse.error(ErrorCode errorCode) 호출
        ApiResponse_legacy<Object> response = ApiResponse_legacy.error(errorCode);
        // 메시지에 상세 에러 추가
        response.setMessage(errorCode.getMessage() + ": " + errorMessage);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * enum type 일치하지 않아 binding 못할 경우 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse_legacy<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_TYPE_VALUE; // INVALID_TYPE_VALUE 사용
        // 변경: ApiResponse.error(ErrorCode errorCode) 호출
        ApiResponse_legacy<Object> response = ApiResponse_legacy.error(errorCode);
        // 메시지에 상세 에러 추가 (원하는 경우)
        response.setMessage(errorCode.getMessage() + ": " + e.getName());


        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * 지원하지 않은 HTTP method 호출 시 발생하는 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse_legacy<Object>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED; // METHOD_NOT_ALLOWED 사용
        // 변경: ApiResponse.error(ErrorCode errorCode) 호출
        ApiResponse_legacy<Object> response = ApiResponse_legacy.error(errorCode);
        // 메시지에 상세 에러 추가
        response.setMessage(errorCode.getMessage() + ": " + e.getMethod());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * Authentication 객체가 필요한 권한을 보유하지 않거나,
     * Spring Security 인증 과정에서 발생하는 예외 (로그인 실패 등) 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ApiResponse_legacy<Object>> handleAuthenticationException(AuthenticationException e) {
        log.error("AuthenticationException caught: {}", e.getMessage());
        log.error("Exception type: {}", e.getClass().getName());

        ErrorCode errorCode;
        if (e instanceof BadCredentialsException || e instanceof UsernameNotFoundException) {
            errorCode = ErrorCode.LOGIN_FAILED;
        } else {
            errorCode = ErrorCode.UNAUTHORIZED;
        }

        // 변경: ApiResponse.error(ErrorCode errorCode) 호출
        ApiResponse_legacy<Object> response = ApiResponse_legacy.error(errorCode);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * 인가(권한) 관련 예외 처리: Spring Security의 AccessDeniedException
     * 이전에 nio.file.AccessDeniedException이었다면, spring security 것으로 변경 필요
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    protected ResponseEntity<ApiResponse_legacy<Object>> handleSpringAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
        log.error("Spring Security AccessDeniedException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.HANDLE_ACCESS_DENIED;
        ApiResponse_legacy<Object> response = ApiResponse_legacy.error(errorCode);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * 커플 관련 에러 처리
     */
    @ExceptionHandler(CoupleException.class)
    protected ResponseEntity<ApiResponse_legacy<Object>> handleTicketException(CoupleException e) {
        log.error("Ticket Exception: {}", e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        ApiResponse_legacy<Object> response = ApiResponse_legacy.error(errorCode);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse_legacy<Object>> handleException(Exception e) {
        log.error("Unhandled Exception: {}", e.getMessage(), e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        // 변경: ApiResponse.error(ErrorCode errorCode) 호출
        ApiResponse_legacy<Object> response = ApiResponse_legacy.error(errorCode);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}