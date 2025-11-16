package com.runningcity.global.response;


import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 모든 도메인에 공통으로 적용되는 전역 코드!
 *  Auth, User, Post 등 도메인별 에러는 각자 자기 도메인별 Enum에,
 *  서버, 인증, 요청 유효성, 권한 등 시스템 전역 수준의 에러는 CommonResponseCode에 둔다.
 */
@Getter
public enum CommonResponseCode implements BaseResponseCode {

    SUCCESS(HttpStatus.OK, "COMMON_2000", "요청이 성공적으로 처리되었습니다."),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_4000", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_4010", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_4030", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_4040", "요청한 리소스를 찾을 수 없습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_4050", "유효하지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_5000", "서버 내부 오류가 발생했습니다."),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_5000", "서버 내부 오류가 발생했습니다."),

    // Onboarding 관련 에러
    ONBOARDING_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "ONBOARDING_4000", "입력값이 올바르지 않습니다."),
    ONBOARDING_ALREADY_COMPLETED(HttpStatus.CONFLICT, "ONBOARDING_4090", "이미 온보딩을 완료한 사용자입니다."),
    ONBOARDING_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "ONBOARDING_4001", "온보딩을 먼저 완료해야 합니다."),
    ONBOARDING_HEART_RATE_WITHOUT_WATCH(HttpStatus.BAD_REQUEST, "ONBOARDING_4002", "스마트워치가 없는 경우 심박수를 입력할 수 없습니다."),

    // User 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_4040", "사용자를 찾을 수 없습니다."),
    USER_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "USER_4000", "입력값이 올바르지 않습니다."),
    NICKNAME_DUPLICATE(HttpStatus.CONFLICT, "USER_4090", "이미 사용 중인 닉네임입니다."),
    NICKNAME_INVALID_LENGTH(HttpStatus.BAD_REQUEST, "USER_4001", "닉네임은 2자 이상 10자 이하여야 합니다."),
    NICKNAME_INVALID_CHARACTER(HttpStatus.BAD_REQUEST, "USER_4002", "닉네임은 한글, 영문, 숫자만 사용 가능합니다."),
    INVALID_CREDIT_AMOUNT(HttpStatus.BAD_REQUEST, "USER_4003", "차감할 크레딧은 0보다 커야 합니다."),
    INSUFFICIENT_CREDIT(HttpStatus.BAD_REQUEST, "USER_4004", "크레딧이 부족합니다."),

    // Mission 관련 에러
    DAILY_MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION_4040", "해당 일일 미션을 찾을 수 없습니다."),
    DAILY_MISSION_ALREADY_CLAIMED(HttpStatus.BAD_REQUEST, "MISSION_4001", "이미 보상을 수령한 미션입니다."),
    DAILY_MISSION_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "MISSION_4002", "미션을 완료하지 않아 보상을 받을 수 없습니다."),

    // Entry 관련 성공 응답
    ENTRY_GET_SUCCESS(HttpStatus.OK, "ENTRY_2000", "잠입 기지 상세 조회에 성공했습니다."),
    ENTRY_GET_LIST_SUCCESS(HttpStatus.OK, "ENTRY_2001", "잠입 기지 목록 조회에 성공했습니다."),
    // Entry 관련 에러
    ENTRY_BAD_REQUEST(HttpStatus.BAD_REQUEST, "ENTRY_4000", "잘못된 잠입 기지 요청입니다."),
    ENTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENTRY_4001", "해당 잠입 기지가 존재하지 않습니다."),

    // 내 사무실
    MY_OFFICE_GET_SUCCESS(HttpStatus.OK, "MY_OFFICE_2000", "내 사무실 조회에 성공했습니다."),
    MY_OFFICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "MY_OFFICE_4001", "내 사무실 요청에 실패했습니다."),

    // 내 사무실 공개 설정
    PRIVACY_SETTING_GET_SUCCESS(HttpStatus.OK, "PRIVACY_SETTING_2000", "내 사무실 공개 설정 조회 요청에 성공했습니다."),
    PRIVACY_SETTING_POST_SUCCESS(HttpStatus.OK, "PRIVACY_SETTING_2001", "내 사무실 공개 설정 저장 요청에 성공했습니다."),
    PRIVACY_SETTING_BAD_REQUEST(HttpStatus.BAD_REQUEST, "PRIVACY_SETTING_4001", "내 사무실 공개 설정 요청에 실패했습니다."),

    // 옷 갈아입히기
    CHANGE_CLOTHES_GET_SUCCESS(HttpStatus.OK, "CHANGE_CLOTHES_2000", "옷 갈아입기 조회 요청에 성공했습니다."),
    CHANGE_CLOTHES_POST_SUCCESS(HttpStatus.OK, "CHANGE_CLOTHES_2001", "옷 갈아입기 저장 요청에 성공했습니다."),
    CHANGE_CLOTHES_BAD_REQUEST(HttpStatus.BAD_REQUEST, "CHANGE_CLOTHES_4001", "옷 갈아입기 요청에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CommonResponseCode(HttpStatus status, String code, String message) {
        this.httpStatus = status;
        this.code = code;
        this.message = message;
    }
}
