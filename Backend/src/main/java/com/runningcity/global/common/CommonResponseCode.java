package com.runningcity.global.common;


import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 모든 도메인에 공통으로 적용되는 전역 코드!
 *  Auth, User, Post 등 도메인별 에러는 각자 자기 도메인별 Enum에,
 *  서버, 인증, 요청 유효성, 권한 등 시스템 전역 수준의 에러는 CommonResponseCode에 둔다.
 */
@Getter
public enum CommonResponseCode implements BaseResponseCode {

    SUCCESS(HttpStatus.OK, "COMMON_2000", "요청이 성공적으로 처리되었습니다."),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_4000", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_4010", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_4030", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_4040", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_5000", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_4050", "유효하지 않은 입력값입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CommonResponseCode(HttpStatus status, String code, String message) {
        this.httpStatus = status;
        this.code = code;
        this.message = message;
    }
}
