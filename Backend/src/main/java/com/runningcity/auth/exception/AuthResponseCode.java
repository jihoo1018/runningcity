package com.runningcity.auth.exception;

import com.runningcity.global.response.BaseResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthResponseCode implements BaseResponseCode {

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_4091", "이미 존재하는 이메일입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_4001", "비밀번호가 일치하지 않습니다."),
    BAD_REQUEST_PARAM(HttpStatus.BAD_REQUEST, "AUTH_4000", "요청 파라미터가 올바르지 않습니다."),
    ENCODING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_5001", "비밀번호 인코딩 중 오류가 발생했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_5000", "회원가입 처리 중 서버 오류가 발생했습니다."),
    UNAUTHORIZED_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_4011", "이메일 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_4041", "등록되지 않은 이메일입니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "AUTH_4031", "비활성화된 계정입니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    AuthResponseCode(HttpStatus status, String code, String message) {
        this.httpStatus = status;
        this.code = code;
        this.message = message;
    }
}
