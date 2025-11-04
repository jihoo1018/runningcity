package com.runningcity.run.exception;

import com.runningcity.global.response.BaseResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RunResponseCode implements BaseResponseCode {

    // 4xx
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "RUN_4040", "세션을 찾을 수 없습니다."),
    SESSION_FINALIZED(HttpStatus.CONFLICT, "RUN_4091", "해당 세션은 이미 확정되어 업로드할 수 없습니다."),
    INVALID_POINTS_PAYLOAD(HttpStatus.BAD_REQUEST, "RUN_4000", "포인트 요청 본문이 올바르지 않습니다."),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "RUN_4130", "요청 본문이 1MB를 초과합니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "RUN_4290", "요청이 과도합니다. 잠시 후 다시 시도하세요."),

    // 5xx
    ROUTE_BUILD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RUN_5001", "경로 생성 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    RunResponseCode(HttpStatus status, String code, String message) {
        this.httpStatus = status;
        this.code = code;
        this.message = message;
    }
}