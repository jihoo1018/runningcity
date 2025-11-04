package com.runningcity.global.exception;

import com.runningcity.global.response.BaseResponseCode;
import com.runningcity.global.response.CommonResponseCode;
import lombok.Getter;

// before: private final CommonResponseCode commonResponseCode;
@Getter
public class BaseException extends RuntimeException {
    private final BaseResponseCode responseCode;

    public BaseException(BaseResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
    public BaseException(BaseResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }
    public BaseException(BaseResponseCode responseCode, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.responseCode = responseCode;
    }
}
