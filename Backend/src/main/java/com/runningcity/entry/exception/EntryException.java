package com.runningcity.entry.exception;


import com.runningcity.global.exception.BaseException;
import com.runningcity.global.response.CommonResponseCode;

public class EntryException extends BaseException {
    
    public EntryException(CommonResponseCode commonResponseCode) {
        super(commonResponseCode);
    }
    
    public EntryException(CommonResponseCode commonResponseCode, String message) {
        super(commonResponseCode, message);
    }

    public EntryException(CommonResponseCode commonResponseCode, Throwable cause) {
        super(commonResponseCode, cause);
    }
}