package com.runningcity.friendship.controller;

import com.runningcity.friendship.dto.*;
import com.runningcity.friendship.service.FriendshipService;
import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    /**
     * 친구 요청 보내기
     * @param userId 사용자 ID
     * @param request 친구 코드가 포함된 요청
     * @return 친구 요청 응답
     */
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FriendshipResponse> sendFriendRequest(
            @RequestParam("userId") Long userId,
            @Valid @RequestBody AddFriendRequest request
    ) {
        FriendshipResponse response = friendshipService.sendFriendRequest(userId, request);
        return ApiResponse.success(CommonResponseCode.SUCCESS, response);
    }

    /**
     * 내 코드 조회
     * @param userId 사용자 ID
     * @return 내 코드
     */
    @GetMapping("/my-code")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<GetMyCodeResponse> getMyCode(
            @RequestParam("userId") Long userId
    ) {
        GetMyCodeResponse response = friendshipService.getMyCode(userId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, response);
    }

    /**
     * 보낸 요청 목록 조회
     * @param userId 사용자 ID
     * @return 보낸 요청 목록
     */
    @GetMapping("/sent-requests")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<GetSentRequestsResponse>> getSentRequests(
            @RequestParam("userId") Long userId
    ) {
        List<GetSentRequestsResponse> response = friendshipService.getSentRequests(userId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, response);
    }

    /**
     * 받은 요청 목록 조회
     * @param userId 사용자 ID
     * @return 받은 요청 목록
     */
    @GetMapping("/received-requests")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<GetReceivedRequestsResponse>> getReceivedRequests(
            @RequestParam("userId") Long userId
    ) {
        List<GetReceivedRequestsResponse> response = friendshipService.getReceivedRequests(userId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, response);
    }

    /**
     * 친구 요청 취소
     * @param userId 사용자 ID
     * @param friendshipId 친구 요청 ID
     */
    @DeleteMapping("/request/{friendshipId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> cancelFriendRequest(
            @RequestParam("userId") Long userId,
            @PathVariable("friendshipId") Long friendshipId
    ) {
        friendshipService.cancelFriendRequest(userId, friendshipId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, null);
    }

    /**
     * 친구 요청 수락
     * @param userId 사용자 ID
     * @param friendshipId 친구 요청 ID
     * @return 수락된 친구 요청 응답
     */
    @PostMapping("/request/{friendshipId}/accept")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<FriendshipResponse> acceptFriendRequest(
            @RequestParam("userId") Long userId,
            @PathVariable("friendshipId") Long friendshipId
    ) {
        FriendshipResponse response = friendshipService.acceptFriendRequest(userId, friendshipId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, response);
    }

    /**
     * 친구 요청 거절
     * @param userId 사용자 ID
     * @param friendshipId 친구 요청 ID
     */
    @PostMapping("/request/{friendshipId}/reject")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> rejectFriendRequest(
            @RequestParam("userId") Long userId,
            @PathVariable("friendshipId") Long friendshipId
    ) {
        friendshipService.rejectFriendRequest(userId, friendshipId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, null);
    }

    /**
     * 친구 삭제
     * @param userId 사용자 ID
     * @param friendId 친구 ID
     */
    @DeleteMapping("/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteFriend(
            @RequestParam("userId") Long userId,
            @PathVariable("friendId") Long friendId
    ) {
        friendshipService.deleteFriend(userId, friendId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, null);
    }

    /**
     * 친구 목록 조회 (랭킹 시스템, total_exp 기준 내림차순)
     * @param userId 사용자 ID
     * @return 친구 랭킹 목록 (현재 사용자 포함)
     */
    @GetMapping("/ranking")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<FriendRankingResponse>> getFriendRankingList(
            @RequestParam("userId") Long userId
    ) {
        List<FriendRankingResponse> response = friendshipService.getFriendRankingList(userId);
        return ApiResponse.success(CommonResponseCode.SUCCESS, response);
    }
}

