package com.runningcity.friendship.exception;

import com.runningcity.global.response.BaseResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum FriendshipResponseCode implements BaseResponseCode {

    // 4xx (클라이언트 오류)
    FRIEND_REQUEST_ALREADY_SENT(HttpStatus.CONFLICT, "FRIEND_4090", "이미 친구 요청을 보냈습니다."),
    FRIEND_ALREADY_EXISTS(HttpStatus.CONFLICT, "FRIEND_4091", "이미 친구입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "FRIEND_4040", "존재하지 않는 사용자입니다."),
    FRIEND_REQUEST_SELF(HttpStatus.BAD_REQUEST, "FRIEND_4000", "자기 자신에게 친구 요청을 보낼 수 없습니다."),

    // 5xx (서버 오류)
    FRIEND_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FRIEND_5000", "친구 요청 중 에러가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    FriendshipResponseCode(HttpStatus status, String code, String message) {
        this.httpStatus = status;
        this.code = code;
        this.message = message;
    }
}

