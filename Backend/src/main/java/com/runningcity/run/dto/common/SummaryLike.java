package com.runningcity.run.dto.common;

public interface SummaryLike {
    Integer getTotalSteps();
    Double  getTotalDistance();
    Integer getTotalCalories();
    Integer getAvgHeartRate();
    Integer getDuration();
    Integer getAvgCadence();
    Integer getAvgPace();
    Double  getElevation();
}
