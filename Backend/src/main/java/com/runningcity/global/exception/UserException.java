package com.runningcity.global.exception;


import com.runningcity.global.response.CommonResponseCode;

public class UserException extends BaseException {
    
    public UserException(CommonResponseCode commonResponseCode) {
        super(commonResponseCode);
    }
    
    public UserException(CommonResponseCode commonResponseCode, String message) {
        super(commonResponseCode, message);
    }

    public UserException(CommonResponseCode commonResponseCode, Throwable cause) {
        super(commonResponseCode, cause);
    }
} 