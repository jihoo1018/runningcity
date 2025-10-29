package com.runningcity.mission.controller;

import com.runningcity.mission.dto.DailyMissionModal;
import com.runningcity.mission.service.DailyMissionService;
import com.runningcity.mission.entity.DailyMission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@CrossOrigin(
  origins = {"http://localhost:5173","http://localhost:3000"},
  allowCredentials = "true"
)
@RestController
@RequestMapping("/daily-mission")
public class DailyMissionController {

    private final DailyMissionService service;

    public DailyMissionController(DailyMissionService service) {
        this.service = service;
    }

    @GetMapping("/modal")
    public ResponseEntity<DailyMissionModal> modal() {
        return ResponseEntity.ok(service.getTodayModal());
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<Void> progress(@PathVariable Long id, @RequestParam double addKm) {
        service.addKm(id, addKm);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<Map<String, Object>> claim(@PathVariable Long id) {
        int reward = service.claim(id);
        return ResponseEntity.ok(Map.of("rewardCoins", reward, "claimed", true));
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        service.resetToday();
        return ResponseEntity.noContent().build();
    }
}
