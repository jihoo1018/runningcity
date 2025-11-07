package com.runningcity.run.controller;

import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.run.dto.*;
import com.runningcity.run.service.RunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/sessions") // (global) context-path: /api/v1
@RequiredArgsConstructor
public class RunController {

    private final RunService runService;
    private static final long userId = 1L; // 로그인 없으니 임시 1 고정

    /** 사후 동기화 업로드 (워치 단독 → 연결 후 1회 업로드) */
    @PostMapping("/watch")
    public ResponseEntity<ApiResponse<Void>> uploadFromWatch(@Valid @RequestBody WatchUploadRequest req
                                                             //  @AuthenticationPrincipal UserDetails userDetails,
                                                             ) {
        // MVP: 인증 없음. 추후 JWT userId 매칭 추가.
        runService.uploadWatchOnce(userId, req);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS));
    }


}
