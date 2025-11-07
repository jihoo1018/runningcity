package com.runningcity.run.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningcity.global.exception.BaseException;
import com.runningcity.run.dto.WatchUploadRequest;
import com.runningcity.run.exception.RunResponseCode;
import com.runningcity.run.repository.RunNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RunService {

    private final RunNativeRepository nativeRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void uploadWatchOnce(long userId, WatchUploadRequest req) {
        // epoch millis 강제 + 범위 검증
        Instant st = toInstantStrictMillis(req.getStartTime());
        Instant et = toInstantStrictMillis(req.getEndTime());

        // 시간 순서 검증
        if (!st.isBefore(et)) {
            throw new BaseException(RunResponseCode.INVALID_TIME_RANGE);
        }

        // GPS 유효성 검증
        validateGps(req.getGpsPoints());

        // 워치 사후 동기화 상수
        final String TYPE = "NORMAL";
        final String DEVICE = "WATCH";
        final Long BASE_ID = null;

        // 세션 UPSERT
        long sid = nativeRepository.upsertSessionAndSummary(
                req.getClientSecretKey(),
                userId,
                st, et,
                TYPE, DEVICE, BASE_ID,
                req.getSummary().getTotalSteps(),
                req.getSummary().getTotalDistance(),
                req.getSummary().getTotalCalories(),
                req.getSummary().getAvgHeartRate(),
                req.getSummary().getDuration(),
                req.getSummary().getAvgCadence(),
                req.getSummary().getAvgPace(),
                req.getSummary().getElevation(),
                toJsonString(req.getCadenceRecords()),
                toJsonString(req.getHeartRateRecords())
        );

        // 포인트 배치 멱등 삽입
        nativeRepository.batchInsertPoints(sid, req.getGpsPoints());

        // 경로 생성/업데이트
        boolean ok = nativeRepository.upsertRouteAndLength(sid, 2.0);
        if (!ok) throw new BaseException(RunResponseCode.ROUTE_BUILD_FAILED);
    }

    /** epoch millis(숫자)만 허용. 범위 밖이면 INVALID_EPOCH_MILLIS */
    private Instant toInstantStrictMillis(long epochMillis) {
        long min = Instant.parse("2000-01-01T00:00:00Z").toEpochMilli();
        long max = Instant.now().plusSeconds(86400).toEpochMilli(); // 지금 + 1일
        if (epochMillis < min || epochMillis > max) {
            throw new BaseException(RunResponseCode.INVALID_EPOCH_MILLIS);
        }
        return Instant.ofEpochMilli(epochMillis);
    }

    /** GPS 리스트와 각 요소의 무결성 검증 */
    private void validateGps(List<WatchUploadRequest.GpsPoint> pts) {
        if (pts == null || pts.isEmpty()) {
            throw new BaseException(RunResponseCode.GPS_POINTS_EMPTY);
        }
        int expected = 1;
        long prevTs = Long.MIN_VALUE;

        for (int i = 0; i < pts.size(); i++) {
            var p = pts.get(i);

            // seq: 1..N 연속
            if (p.getSeq() != expected) {
                throw new BaseException(RunResponseCode.GPS_SEQ_OUT_OF_ORDER);
            }
            expected++;

            // createdAt: 오름차순
            if (p.getCreatedAt() < prevTs) {
                throw new BaseException(RunResponseCode.GPS_TIME_NOT_ASC);
            }
            prevTs = p.getCreatedAt();

            // NaN 금지
            if (Double.isNaN(p.getLatitude()) || Double.isNaN(p.getLongitude())
                    || (p.getAltitude() != null && Double.isNaN(p.getAltitude()))
                    || (p.getSpeed() != null && Double.isNaN(p.getSpeed()))) {
                throw new BaseException(RunResponseCode.GPS_VALUE_NAN);
            }

            // 좌표 범위
            if (p.getLatitude() < -90 || p.getLatitude() > 90
                    || p.getLongitude() < -180 || p.getLongitude() > 180) {
                throw new BaseException(RunResponseCode.GPS_COORD_OUT_OF_RANGE);
            }
        }
    }

    /** JSON 직렬화 실패 시 JSON_SERIALIZATION_FAILED */
    private String toJsonString(Object v) {
        if (v == null) return null; // SQL NULL
        try {
            return objectMapper.writeValueAsString(v);
        } catch (Exception e) {
            throw new BaseException(RunResponseCode.JSON_SERIALIZATION_FAILED);
        }
    }
}
