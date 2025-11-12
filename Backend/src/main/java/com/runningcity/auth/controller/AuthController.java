package com.runningcity.auth.controller;

import com.runningcity.auth.dto.CheckEmailResponse;
import com.runningcity.auth.dto.SignupRequest;
import com.runningcity.auth.service.AuthService;
import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ApiResponse<CheckEmailResponse> checkEmail(@RequestParam("email") String email) {
        CheckEmailResponse resp = authService.checkEmail(email);
        return ApiResponse.success(CommonResponseCode.SUCCESS, resp);
    }

    // 회원가입
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody @Valid SignupRequest req) {
        authService.signup(req);
        return ApiResponse.success(CommonResponseCode.SUCCESS);
    }
}
