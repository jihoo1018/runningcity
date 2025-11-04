package com.runningcity.run.controller;

import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.run.dto.CreateSessionRequest;
import com.runningcity.run.dto.CreateSessionResponse;
import com.runningcity.run.dto.FinishRequest;
import com.runningcity.run.dto.FinishResponse;
import com.runningcity.run.dto.UploadPointsRequest;
import com.runningcity.run.dto.UploadPointsResponse;
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

    /**
     * 세션 생성
     * - POST /api/v1/sessions
     * - Response: 201 Created + { sessionId, startAt, status }
     * - 인증: @AuthenticationPrincipal 로 받은 userDetails에서 userId 추출 → 서비스에 전달
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSessionResponse>> createSession(
           // @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateSessionRequest req
    ) {
        //long userId = resolveUserId(userDetails);
        CreateSessionResponse data = runService.createSession(userId, req);
        ApiResponse<CreateSessionResponse> body = ApiResponse.success(CommonResponseCode.SUCCESS, data);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * 포인트 배치 업로드(멱등 + 연속 ACK)
     * - POST /api/v1/sessions/{sid}/points
     * - Response: 200 OK + { ackedUntilSeq, maxInsertedSeq }
     * - 인증: @AuthenticationPrincipal → userId 전달(서비스에서 세션 소유자 검증)
     */
    @PostMapping("/{sid}/points")
    public ResponseEntity<ApiResponse<UploadPointsResponse>> uploadPoints(
            //@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable long sid,
            @Valid @RequestBody UploadPointsRequest req
    ) {
        //long userId = resolveUserId(userDetails);
        UploadPointsResponse data = runService.uploadPoints(userId, sid, req);
        ApiResponse<UploadPointsResponse> body = ApiResponse.success(CommonResponseCode.SUCCESS, data);
        return ResponseEntity.ok(body);
    }

    /**
     * 세션 종료(Finish, 멱등)
     * - POST /api/v1/sessions/{sid}/finish
     * - Response: FINALIZED=200, CLOSING=202
     * - 인증: @AuthenticationPrincipal → userId 전달(서비스에서 세션 소유자 검증)
     */
    @PostMapping("/{sid}/finish")
    public ResponseEntity<ApiResponse<FinishResponse>> finish(
          //  @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable long sid,
            @Valid @RequestBody FinishRequest req
    ) {
        // long userId = resolveUserId(userDetails);
        FinishResponse data = runService.finish(userId, sid, req);
        ApiResponse<FinishResponse> body = ApiResponse.success(CommonResponseCode.SUCCESS, data);

        if ("FINALIZED".equals(data.getStatus())) { //200
            return ResponseEntity.ok(body);
        }
        // CLOSING 202
        long seconds = 60L;
        if (data.getClosingDeadline() != null) {
            long remain = Duration.between(Instant.now(), data.getClosingDeadline()).getSeconds();
            seconds = Math.max(1, remain);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(seconds))
                .body(body);
    }

}
