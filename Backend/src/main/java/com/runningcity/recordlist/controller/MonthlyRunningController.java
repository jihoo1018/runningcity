package com.runningcity.recordlist.controller;

import com.runningcity.recordlist.dto.MonthlyRunningResponse;
import com.runningcity.recordlist.service.MonthlyRunningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recordlist")
public class MonthlyRunningController {

    private final MonthlyRunningService service;

    public MonthlyRunningController(MonthlyRunningService service) {
        this.service = service;
    }

    @GetMapping("/month")
    public MonthlyRunningResponse getMonthly(
            @RequestParam String userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return service.getMonthly(userId, year, month);
    }
}
