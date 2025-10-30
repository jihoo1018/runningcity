package com.runningcity.global.exception;

import com.runningcity.global.response.CommonResponseCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final CommonResponseCode commonResponseCode;

    public BaseException(CommonResponseCode commonResponseCode) {
        super(commonResponseCode.getMessage());
        this.commonResponseCode = commonResponseCode;
    }

    public BaseException(CommonResponseCode commonResponseCode, String message) {
        super(message);
        this.commonResponseCode = commonResponseCode;
    }

    public BaseException(CommonResponseCode commonResponseCode, Throwable cause) {
        super(commonResponseCode.getMessage(), cause);
        this.commonResponseCode = commonResponseCode;
    }
} 