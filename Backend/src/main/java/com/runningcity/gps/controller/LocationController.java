package com.runningcity.gps.controller;

import com.runningcity.gps.dto.LocationData;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final List<LocationData> locations = new ArrayList<>();

    @PostMapping
    public Map<String, Object> receiveLocation(@RequestBody LocationData data) {
        System.out.println("📍 Received: " + data);
        locations.add(data);
        return Map.of("success", true);
    }

    @GetMapping
    public Map<String, Object> getLocations() {
        return Map.of("success", true, "data", locations);
    }
}
