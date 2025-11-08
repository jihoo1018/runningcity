package com.runningcity.run.dto.common;

public interface GpsPointLike {
    int getSeq();
    double getLatitude();
    double getLongitude();
    Long getCreatedAt();
    Double getAltitude();
    Double getSpeed();
}