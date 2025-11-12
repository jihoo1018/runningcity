package com.runningcity.auth.controller;

import com.runningcity.auth.dto.CheckEmailResponse;
import com.runningcity.auth.dto.LoginRequest;
import com.runningcity.auth.dto.LoginSuccessData;
import com.runningcity.auth.dto.SignupRequest;
import com.runningcity.auth.service.AuthService;
import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<CheckEmailResponse>> checkEmail(
            @RequestParam("email") String email
    ) {
        CheckEmailResponse resp = authService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, resp));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequest req
    ) {
        authService.signup(req);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginSuccessData>> login(
            @Valid @RequestBody LoginRequest req
    ) {
        LoginSuccessData data = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, data));
    }
}
