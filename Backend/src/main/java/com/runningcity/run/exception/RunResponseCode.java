package com.runningcity.run.exception;

import com.runningcity.global.response.BaseResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RunResponseCode implements BaseResponseCode {

    // 4xx (클라이언트 오류)
    SESSION_NOT_FOUND        (HttpStatus.NOT_FOUND,           "RUN_4040", "세션을 찾을 수 없습니다."),
    SESSION_FINALIZED        (HttpStatus.CONFLICT,            "RUN_4091", "해당 세션은 이미 확정되어 업로드할 수 없습니다."),
    PAYLOAD_TOO_LARGE        (HttpStatus.PAYLOAD_TOO_LARGE,   "RUN_4130", "요청 본문이 1MB를 초과합니다."),
    TOO_MANY_REQUESTS        (HttpStatus.TOO_MANY_REQUESTS,   "RUN_4290", "요청이 과도합니다. 잠시 후 다시 시도하세요."),

    // 입력/포맷/시퀀스 검증 (정확한 사유 코드로 분리)
    INVALID_TIME_RANGE       (HttpStatus.BAD_REQUEST,         "RUN_4001", "startTime은 endTime보다 과거여야 합니다."),
    INVALID_EPOCH_MILLIS     (HttpStatus.BAD_REQUEST,         "RUN_4002", "시간 값은 유효한 epoch 밀리초 범위여야 합니다."),
    GPS_POINTS_EMPTY         (HttpStatus.BAD_REQUEST,         "RUN_4003", "gpsPoints는 비어 있을 수 없습니다."),
    GPS_SEQ_OUT_OF_ORDER     (HttpStatus.BAD_REQUEST,         "RUN_4004", "gpsPoints.seq는 1부터 1씩 증가해야 합니다."),
    GPS_TIME_NOT_ASC         (HttpStatus.BAD_REQUEST,         "RUN_4005", "gpsPoints.createdAt 값은 오름차순이어야 합니다."),
    GPS_VALUE_NAN            (HttpStatus.BAD_REQUEST,         "RUN_4006", "gpsPoints에 NaN 값은 허용되지 않습니다."),
    GPS_COORD_OUT_OF_RANGE   (HttpStatus.BAD_REQUEST,         "RUN_4007", "위도/경도 값이 허용 범위를 벗어났습니다."),
    JSON_SERIALIZATION_FAILED(HttpStatus.BAD_REQUEST,         "RUN_4008", "요청 본문 직렬화(JSON) 실패가 발생했습니다."),

    // 5xx (서버 오류)
    ROUTE_BUILD_FAILED       (HttpStatus.INTERNAL_SERVER_ERROR,"RUN_5001", "경로 생성 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    RunResponseCode(HttpStatus status, String code, String message) {
        this.httpStatus = status;
        this.code = code;
        this.message = message;
    }
}
