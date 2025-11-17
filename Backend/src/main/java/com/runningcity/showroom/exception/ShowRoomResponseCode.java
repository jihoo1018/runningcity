package com.runningcity.showroom.exception;

import com.runningcity.global.response.BaseResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ShowRoomResponseCode implements BaseResponseCode {
    //성공
    SHOWROOM_SUCCESS  (HttpStatus.OK , "ShowRoom2000","쇼룸 조회가 성공적으로 이루어졌습니다."),
    GLOBAL_SHOWROOM_SUCCESS(HttpStatus.OK, "ShowRoom2001", "글로벌 쇼룸 조회가 성공적으로 이루어졌습니다."),
    FRIEND_SHOWROOM_SUCCESS(HttpStatus.OK, "ShowRoom2002", "친구 쇼룸 조회가 성공적으로 이루어졌습니다."),

    // ✅ 에러 추가
    NO_FRIENDS_FOUND(HttpStatus.NOT_FOUND, "ShowRoom4001", "친구가 없습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ShowRoomResponseCode(HttpStatus status, String code, String message) {
        this.httpStatus = status;
        this.code = code;
        this.message = message;
    }


}
