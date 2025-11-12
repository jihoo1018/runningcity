package com.runningcity.report.service;

import com.runningcity.global.exception.BaseException;
import com.runningcity.report.repository.ReportNativeRepository;
import com.runningcity.run.entity.RunSession;
import lombok.*;
import org.springframework.stereotype.Service;

import com.runningcity.report.dto.ReportResponse;

import com.runningcity.report.repository.ReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.runningcity.report.dto.ReportDetailResponse;
import com.runningcity.report.exception.ReportResponseCode;
import org.springframework.transaction.annotation.Transactional;


import java.time.*;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportNativeRepository nativeRepository;


    public ReportResponse getMonthly(Long userId, int year, int month) {
        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate firstNext = first.plusMonths(1);

        // 조회 범위만 Instant로 통일
        Instant start = first.atStartOfDay(KST).toInstant();
        Instant end   = first.plusMonths(1).atStartOfDay(KST).toInstant();


        List<RunSession> sessions = reportRepository.findMonthlySessions(userId, start, end);

        // calendar
        LocalDate endDate = first.withDayOfMonth(first.lengthOfMonth());
        Map<Integer, Boolean> hasRecordByDay = new HashMap<>();
        for (RunSession rs : sessions) {
            LocalDate d = rs.getStartTime().atZone(KST).toLocalDate();
            hasRecordByDay.put(d.getDayOfMonth(), true);
        }
        List<ReportResponse.CalendarDay> calendarDays = new ArrayList<>();
        for (int d = 1; d <= endDate.getDayOfMonth(); d++) {
            calendarDays.add(new ReportResponse.CalendarDay(d, hasRecordByDay.getOrDefault(d, false)));
        }

        // summary
        double totalDistanceKm = sessions.stream()
                .mapToDouble(rs -> rs.getTotalDistance() != null ? rs.getTotalDistance() / 1000.0 : 0.0)
                .sum();
        int totalRuns = sessions.size();
        int avgPaceSec = 0;
        if (totalRuns > 0) {
            avgPaceSec = (int) sessions.stream()
                    .mapToInt(rs -> rs.getAvgPace() != null ? rs.getAvgPace() : 0)
                    .average()
                    .orElse(0);
        }
        ReportResponse.MonthSummary summary = new ReportResponse.MonthSummary(
                round1(totalDistanceKm),
                totalRuns,
                toPaceString(avgPaceSec)
        );

        // records
        List<ReportResponse.RunningRecordDto> records = sessions.stream()
                .map(rs -> new ReportResponse.RunningRecordDto(
                        rs.getSessionId(),
                        rs.getStartTime().atZone(KST).toLocalDate().toString(),
                        rs.getTotalDistance() != null ? rs.getTotalDistance() / 1000.0 : 0.0,
                        toPaceString(rs.getAvgPace()),
                        toTimeString(rs.getDuration()),
                        rs.getType()
                ))
                .toList();

        return new ReportResponse(
                year,
                month,
                summary,
                calendarDays,
                records
        );
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static String toPaceString(Integer sec) {
        if (sec == null || sec == 0) return "0'00";
        int m = sec / 60;
        int s = sec % 60;
        return String.format("%d'%02d", m, s);
    }

    private static String toTimeString(Integer sec) {
        if (sec == null) return "00:00:00";
        int h = sec / 3600;
        int m = (sec % 3600) / 60;
        int s = sec % 60;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        return String.format("0:%02d:%02d", m, s);
    }

    /** 단건 상세 */
    @Transactional(readOnly = true)
    public ReportDetailResponse getReportDetail(long userId, long sessionId) {

        RunSession rs = reportRepository.findFinalizedByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BaseException(ReportResponseCode.REPORT_SESSION_NOT_FOUND));

        // summary 매핑
        ReportDetailResponse.Summary summary = ReportDetailResponse.Summary.builder()
                .totalSteps(rs.getTotalSteps())
                .totalDistance(rs.getTotalDistance())
                .totalCalories(rs.getTotalCalories())
                .avgHeartRate(rs.getAvgHeartRate())
                .duration(rs.getDuration())
                .avgCadence(rs.getAvgCadence())
                .avgPace(rs.getAvgPace())
                .elevation(rs.getElevation())
                .build();

        // rewards_meta → credit/exp만 추출(없으면 null)
        ReportDetailResponse.Rewards rewards = extractRewards(rs.getRewardsMeta());

        // route GeoJSON (없으면 null)
        Optional<String> geojsonOpt = nativeRepository.findRouteGeoJson(sessionId);
        ReportDetailResponse.Route route = geojsonOpt
                .filter(s -> !s.isBlank())
                .map(s -> ReportDetailResponse.Route.builder().geojson(s).build())
                .orElse(null);

        return ReportDetailResponse.builder()
                .type(rs.getType())          // "NORMAL" | "ENTRY"
                .summary(summary)
                .rewards(rewards)
                .route(route)
                .build();
    }

    private ReportDetailResponse.Rewards extractRewards(JsonNode rewardsMeta) {
        if (rewardsMeta == null || rewardsMeta.isNull()) return null;

        Integer credit = null, exp = null;
        if (rewardsMeta.hasNonNull("credit")) {
            credit = safeInt(rewardsMeta.get("credit"));
        }
        if (rewardsMeta.hasNonNull("exp")) {
            exp = safeInt(rewardsMeta.get("exp"));
        }
        if (credit == null && exp == null) return null;

        return ReportDetailResponse.Rewards.builder()
                .credit(credit)
                .exp(exp)
                .build();
    }

    private Integer safeInt(JsonNode node) {
        try {
            if (node == null || node.isNull()) return null;
            if (node.isInt()) return node.intValue();
            if (node.isNumber()) return node.numberValue().intValue();
            if (node.isTextual()) return Integer.valueOf(node.textValue());
            return null;
        } catch (Exception e) {
            return null;
        }
    }


}
