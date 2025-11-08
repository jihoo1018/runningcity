
package com.runningcity.run.util;

import com.runningcity.global.exception.BaseException;
import com.runningcity.run.dto.common.GpsPointLike;
import com.runningcity.run.exception.RunResponseCode;

import java.time.Instant;
import java.util.Collection;

public final class RunValidators {
    private RunValidators() {}

    /** epoch millis(숫자)만 허용. 범위 밖이면 INVALID_EPOCH_MILLIS */
    public static Instant toInstantStrictMillis(long epochMillis) {
        long min = Instant.parse("2000-01-01T00:00:00Z").toEpochMilli();
        long max = Instant.now().plusSeconds(86400).toEpochMilli(); // 지금 + 1일
        if (epochMillis < min || epochMillis > max) {
            throw new BaseException(RunResponseCode.INVALID_EPOCH_MILLIS);
        }
        return Instant.ofEpochMilli(epochMillis);
    }

    /** GPS 리스트와 각 요소의 무결성 검증 (DTO 불문, 인터페이스 기준) */
    public static void validateGps(Collection<? extends GpsPointLike> pts) {
        if (pts == null || pts.isEmpty()) {
            throw new BaseException(RunResponseCode.GPS_POINTS_EMPTY);
        }
        int expected = 1;
        long prevTs = Long.MIN_VALUE;

        int i = 0;
        for (GpsPointLike p : pts) {
            if (p.getSeq() != expected) {
                throw new BaseException(RunResponseCode.GPS_SEQ_OUT_OF_ORDER);
            }
            expected++;

            Long ts = p.getCreatedAt();
            if (ts == null || ts < prevTs) {
                throw new BaseException(RunResponseCode.GPS_TIME_NOT_ASC);
            }
            prevTs = ts;

            if (Double.isNaN(p.getLatitude()) || Double.isNaN(p.getLongitude())
                    || (p.getAltitude() != null && Double.isNaN(p.getAltitude()))
                    || (p.getSpeed() != null && Double.isNaN(p.getSpeed()))) {
                throw new BaseException(RunResponseCode.GPS_VALUE_NAN);
            }

            if (p.getLatitude() < -90 || p.getLatitude() > 90
                    || p.getLongitude() < -180 || p.getLongitude() > 180) {
                throw new BaseException(RunResponseCode.GPS_COORD_OUT_OF_RANGE);
            }
            i++;
        }
    }
}
