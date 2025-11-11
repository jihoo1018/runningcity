package com.runningcity.report.service;

import org.springframework.stereotype.Service;

import com.runningcity.report.dto.ReportResponse;
import com.runningcity.report.entity.Report;
import com.runningcity.report.repository.ReportRepository;

import java.time.*;
import java.util.*;

@Service
public class ReportService {

    private final ReportRepository repository;

    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public ReportResponse getMonthly(String userId, int year, int month) {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate firstNext = first.plusMonths(1);

        OffsetDateTime start = first.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime end = firstNext.atStartOfDay(zone).toOffsetDateTime();

        List<Report> sessions = repository.findMonthlySessions(userId, start, end);

        // calendar
        LocalDate endDate = first.withDayOfMonth(first.lengthOfMonth());
        Map<Integer, Boolean> hasRecordByDay = new HashMap<>();
        for (Report rs : sessions) {
            LocalDate d = rs.getStartTime().atZoneSameInstant(zone).toLocalDate();
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
                        rs.getStartTime().atZoneSameInstant(zone).toLocalDate().toString(),
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
}
