package com.runningcity.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
    private List<ErrorDetail> details; // 필드별 상세 에러 (optional)
}