package com.runningcity.report.controller;

import com.runningcity.global.response.ApiResponse;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.report.dto.ReportDetailResponse;
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
    private static final long userId = 1L;


    @GetMapping    // ← /report?userId=...&year=...&month=...
    public ReportResponse getMonthly(
            @RequestParam int year,
            @RequestParam int month
            // ,@AuthenticationPrincipal UserDetails userDetails
    ) {
        return reportService.getMonthly(userId, year, month);
    }


    @GetMapping("/{sid}")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> getReportDetail(
            @PathVariable("sid") Long sessionId
            // ,@AuthenticationPrincipal(expression = "userId") Long userId // 프로젝트에 맞춰 수정
    ) {

        ReportDetailResponse data = reportService.getReportDetail(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(CommonResponseCode.SUCCESS, data));
    }
}

