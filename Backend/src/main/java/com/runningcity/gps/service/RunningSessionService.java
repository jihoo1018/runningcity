package com.runningcity.gps.service;

import com.runningcity.gps.dto.RunningSessionRequest;
import com.runningcity.gps.dto.RunningSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class RunningSessionService {

    private static final double TOLERANCE_RATE = 0.02; // 허용 오차 ±2%
    private static final double EARTH_RADIUS = 6371000.0; // m

    public RunningSessionResponse validateAndSaveSession(RunningSessionRequest req) {

        // 1️⃣ 서버에서 거리 재계산
        double recalculatedDistance = calculateTotalDistance(req.getRoute());
        double recalculatedKm = recalculatedDistance / 1000.0;

        // 2️⃣ 서버에서 속도/페이스 재계산
        long duration = req.getEndTime() - req.getStartTime();
        double avgSpeed = calculateAvgSpeed(recalculatedKm, duration);
        double avgPace = calculateAvgPace(duration, recalculatedKm);

        // 3️⃣ 클라이언트 데이터와 비교
        boolean valid = Math.abs(req.getTotalDistance() - recalculatedKm) / recalculatedKm < TOLERANCE_RATE;

        String message = valid
                ? "✅ 검증 성공: 데이터 일치"
                : "⚠️ 검증 실패: 거리 차이 " +
                String.format("%.2f", Math.abs(req.getTotalDistance() - recalculatedKm)) + " km";

        // 4️⃣ DB 저장 로직은 여기서 추가 가능 (예: repository.save(...))

        return RunningSessionResponse.builder()
                .valid(valid)
                .clientDistance(req.getTotalDistance())
                .serverDistance(recalculatedKm)
                .serverAvgSpeed(avgSpeed)
                .serverAvgPace(avgPace)
                .message(message)
                .build();
    }

    // -------------------------------------
    // 📏 거리 계산 (Haversine formula)
    // -------------------------------------
    private double calculateTotalDistance(List<RunningSessionRequest.RunningPointRequest> points) {
        double total = 0.0;
        for (int i = 1; i < points.size(); i++) {
            total += calculateDistance(
                    points.get(i-1).getLatitude(), points.get(i-1).getLongitude(),
                    points.get(i).getLatitude(), points.get(i).getLongitude()
            );
        }
        return total;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double calculateAvgPace(long durationMillis, double distanceKm) {
        if (distanceKm <= 0.0) return 0.0;
        double minutes = durationMillis / 60000.0;
        return minutes / distanceKm;
    }

    private double calculateAvgSpeed(double distanceKm, long durationMillis) {
        if (durationMillis <= 0.0) return 0.0;
        double hours = durationMillis / 3600000.0;
        return distanceKm / hours;
    }
}