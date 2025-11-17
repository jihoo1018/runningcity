package com.runningcity.run.controller;

import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.report.service.AiReportService;
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
    private final AiReportService aiReportService;
    // private static final long userId = 1L; // 로그인 없으니 임시 1 고정

    // RunController에 추가
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSessionResponse>> createSession(
            @Valid @RequestBody CreateSessionRequest req,
            @RequestParam Long userId
            // @AuthenticationPrincipal UserDetails userDetails
    ) {
        CreateSessionResponse resp = runService.createSession(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(CommonResponseCode.SUCCESS, resp));
    }

    /** 세션 종료 (모바일에서 Finish + 보상 1회 처리) */
    @PostMapping("/{sid}/finish")
    public ResponseEntity<ApiResponse<AiReportResponse>> finish(
            @PathVariable("sid") long sessionId,
            @Valid @RequestBody FinishRequest req,
            @RequestParam Long userId
    ) {
        runService.finishSession(userId, sessionId, req);
        AiReportResponse aiReport = new AiReportResponse(aiReportService.generateAndSave(userId, sessionId, req));
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, aiReport));
    }




    @PostMapping("/watch")
    public ResponseEntity<ApiResponse<Void>> uploadFromWatch(@Valid @RequestBody WatchUploadRequest req,
                                                             @RequestParam Long userId
                                                             //  @AuthenticationPrincipal UserDetails userDetails,
                                                             ) {
        // MVP: 인증 없음. 추후 JWT userId 매칭 추가.
        long sessionId = runService.uploadWatchOnce(userId, req);
        aiReportService.generateAndSave(userId, sessionId, req);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS));
    }


}
