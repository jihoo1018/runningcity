package com.runningcity.global.response;

import org.springframework.http.HttpStatus;

public interface BaseResponseCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}
