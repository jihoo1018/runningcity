package com.runningcity.report.controller;

import com.runningcity.global.client.gms.OpenAiClient;
import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.report.dto.ReportDetailResponse;
import com.runningcity.report.service.AiReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.runningcity.report.dto.ReportResponse;
import com.runningcity.report.service.ReportService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/report")   // ← 여기만 바꿈
public class ReportController {

    private final ReportService reportService;
    // private static final long userId = 1L;
    private final AiReportService aiReportService;


    @GetMapping    // ← /report?userId=...&year=...&month=...
    public ReportResponse getMonthly(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month
            // ,@AuthenticationPrincipal UserDetails userDetails
    ) {
        return reportService.getMonthly(userId, year, month);
    }


    @GetMapping("/{sid}")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> getReportDetail(
            @RequestParam Long userId,
            @PathVariable("sid") Long sessionId
            // ,@AuthenticationPrincipal(expression = "userId") Long userId // 프로젝트에 맞춰 수정
    ) {

        ReportDetailResponse data = reportService.getReportDetail(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, data));
    }

    /**
     * AI 리포트만 생성하는 엔드포인트 (런 세션 종료시 생성 실패한 경우)
     * */
    @PostMapping("/{sid}/ai")
    public ResponseEntity<ApiResponse<ReportDetailResponse.AiReport>> regenerateAiReport(
            @RequestParam Long userId,
            @PathVariable("sid") Long sessionId
    ) {
        String content = aiReportService.generateFromSession(userId, sessionId);

        ReportDetailResponse.AiReport dto = (content != null) ? new ReportDetailResponse.AiReport(content) : null;

        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, dto));
    }
}

