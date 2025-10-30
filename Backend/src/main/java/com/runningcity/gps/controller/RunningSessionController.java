package com.runningcity.gps.controller;

import com.runningcity.gps.dto.RunningSessionRequest;
import com.runningcity.gps.dto.RunningSessionResponse;
import com.runningcity.gps.service.RunningSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/running/session")
@RequiredArgsConstructor
public class RunningSessionController {

    private final RunningSessionService runningSessionService;

    @PostMapping
    public RunningSessionResponse uploadSession(@RequestBody RunningSessionRequest request) {
        return runningSessionService.validateAndSaveSession(request);
    }

    // 선택: 세션 상세 조회용 엔드포인트
    // @GetMapping("/{id}")
    // public RunningSessionResponse getSession(@PathVariable Long id) { ... }
}
