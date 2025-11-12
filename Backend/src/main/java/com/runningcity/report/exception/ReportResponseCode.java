package com.runningcity.report.exception;

import com.runningcity.global.response.BaseResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReportResponseCode implements BaseResponseCode {

    REPORT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_4040", "리포트 세션을 찾을 수 없습니다."),
    REPORT_FORBIDDEN(HttpStatus.FORBIDDEN, "REPORT_4030", "해당 리포트에 접근 권한이 없습니다."),
    REPORT_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT_5000", "리포트 조회 중 서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ReportResponseCode(HttpStatus status, String code, String message) {
        this.httpStatus = status;
        this.code = code;
        this.message = message;
    }
}
