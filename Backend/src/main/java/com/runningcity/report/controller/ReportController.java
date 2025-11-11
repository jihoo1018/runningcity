package com.runningcity.report.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.runningcity.report.dto.ReportResponse;
import com.runningcity.report.service.ReportService;

@RestController
@RequestMapping("/report")   // ← 여기만 바꿈
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping    // ← /report?userId=...&year=...&month=...
    public ReportResponse getMonthly(
            @RequestParam String userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return service.getMonthly(userId, year, month);
    }
}
