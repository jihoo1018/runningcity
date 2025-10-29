package com.runningcity.user.controller;

import com.runningcity.global.common.ApiResponse;
import com.runningcity.global.common.CommonResponseCode;
import com.runningcity.user.dto.NicknameUpdateRequest;
import com.runningcity.user.dto.NicknameUpdateResponse;
import com.runningcity.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 닉네임 수정 API
     * @param userId 사용자 ID
     * @param request 닉네임 수정 요청
     * @return 수정된 사용자 정보 (userId, nickname)
     */
    @PostMapping("/nickname")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NicknameUpdateResponse> updateNickname(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody NicknameUpdateRequest request
            // TODO: JWT에서 userId 추출하여 PathVariable과 일치하는지 검증 필요
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        NicknameUpdateResponse response = userService.updateNickname(userId, request);
        
        return ApiResponse.success(
                CommonResponseCode.SUCCESS,
                response
        );
    }
}

