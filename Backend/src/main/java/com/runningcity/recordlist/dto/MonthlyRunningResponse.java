package com.runningcity.recordlist.dto;

import java.util.List;

public record MonthlyRunningResponse(
        int year,
        int month,
        MonthSummary monthSummary,
        List<CalendarDay> calendarDays,
        List<RunningRecordDto> records
) {
    public record MonthSummary(
            double totalDistanceKm,
            int totalRuns,
            String avgPace
    ) {}

    public record CalendarDay(
            int day,
            boolean hasRecord
    ) {}

    public record RunningRecordDto(
            String date,
            double distanceKm,
            String avgPace,
            String runningTime,
            String runningType
    ) {}
}
