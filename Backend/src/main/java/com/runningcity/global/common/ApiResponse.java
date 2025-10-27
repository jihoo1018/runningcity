package com.runningcity.global.common;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Builder
public class ApiResponse<T> {
    private HttpStatus status; //
    private String code;            // ex) "SUCCESS", "USER_NOT_FOUND"
    private String message;         // ex) "요청이 성공적으로 처리되었습니다."
    private T data;
    private ErrorResponse error; // 실패 시 사용

    // 성공 응답
    public static <T> ApiResponse<T> success(BaseResponseCode code, T data) {
        return ApiResponse.<T>builder()
                .status(code.getHttpStatus())
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .build();
    }

    // 성공(데이터 없음)
    public static ApiResponse<Void> success(BaseResponseCode code) {
        return ApiResponse.<Void>builder()
                .status(code.getHttpStatus())
                .code(code.getCode())
                .message(code.getMessage())
                .build();
    }

    // 실패 응답
    public static ApiResponse<Void> fail(BaseResponseCode code) {
        return ApiResponse.<Void>builder()
                .status(code.getHttpStatus())
                .code(code.getCode())
                .message(code.getMessage())
                .error(new ErrorResponse(null))
                .build();
    }

    // 실패 응답 (필드별 오류 포함)
    public static ApiResponse<Void> fail(BaseResponseCode code, List<ErrorDetail> details) {
        return ApiResponse.<Void>builder()
                .status(code.getHttpStatus())
                .code(code.getCode())
                .message(code.getMessage())
                .error(new ErrorResponse(details))
                .build();
    }
}
