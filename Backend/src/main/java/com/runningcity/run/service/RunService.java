package com.runningcity.run.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runningcity.global.exception.BaseException;
import com.runningcity.run.dto.CreateSessionRequest;
import com.runningcity.run.dto.CreateSessionResponse;
import com.runningcity.run.dto.FinishRequest;
import com.runningcity.run.dto.WatchUploadRequest;
import com.runningcity.run.exception.RunResponseCode;
import com.runningcity.run.repository.RunNativeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static com.runningcity.run.util.RunValidators.toInstantStrictMillis;
import static com.runningcity.run.util.RunValidators.validateGps;

@Service
@RequiredArgsConstructor
public class RunService {

    private final RunNativeRepository nativeRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public CreateSessionResponse createSession(long userId, CreateSessionRequest req) {

        // baseId는 NORMAL/ENTRY 모두 nullable 허용
        long sid = nativeRepository.createSession(
                userId, req.getType(), req.getDeviceType(), req.getBaseId()
        );

        return CreateSessionResponse.builder().sessionId(sid).build();
    }

    /** 세션 종료 (모바일 Finish) — 서비스는 Instant만 사용 */
    @Transactional
    public void finishSession(long userId, long sid, FinishRequest req) {
        Instant st = toInstantStrictMillis(req.getStartTime());
        Instant et = toInstantStrictMillis(req.getEndTime());
        if (!st.isBefore(et)) {
            throw new BaseException(RunResponseCode.INVALID_TIME_RANGE);
        }

        // ✅ GPS 검증 (null/빈 리스트는 validateGps 내부에서 그냥 통과)
        List<? extends com.runningcity.run.dto.common.GpsPointLike> gps = req.getGpsPoints();
        validateGps(gps);

        String rewardsJson = (req.getRewards() == null) ? null : toJsonString(req.getRewards());

        nativeRepository.finishUpdateSessionAndMaybeApplyRewards(
                sid,
                st,
                et,
                req.getSummary().getTotalSteps(),
                req.getSummary().getTotalDistance(),
                req.getSummary().getTotalCalories(),
                req.getSummary().getAvgHeartRate(),
                req.getSummary().getDuration(),
                req.getSummary().getAvgCadence(),
                req.getSummary().getAvgPace(),
                req.getSummary().getElevation(),
                toJsonString(req.getCadenceRecords()),
                toJsonString(req.getHeartRateRecords()),
                rewardsJson,
                req.getClientSecretKey()
        );

        // ✅ GPS가 1개 이상 있을 때만 포인트/라인 처리
        if (gps != null && !gps.isEmpty()) {
            nativeRepository.batchInsertPoints(sid, gps);

            // 라인은 2개 이상일 때만 만드는 게 안전하니 size 체크
            if (gps.size() >= 2) {
                boolean ok = nativeRepository.upsertRouteAndLength(sid, 2.0);
                if (!ok) throw new BaseException(RunResponseCode.ROUTE_BUILD_FAILED);
            }
        }
    }

    /** 워치 사후 동기화 업로드 */
    @Transactional
    public long uploadWatchOnce(long userId, WatchUploadRequest req) {
        // epoch millis 강제 + 범위 검증
        Instant st = toInstantStrictMillis(req.getStartTime());
        Instant et = toInstantStrictMillis(req.getEndTime());

        // 시간 순서 검증
        if (!st.isBefore(et)) {
            throw new BaseException(RunResponseCode.INVALID_TIME_RANGE);
        }

        // GPS 유효성 검증 (null/빈 리스트는 validateGps 내부에서 통과)
        List<WatchUploadRequest.GpsPoint> gps = req.getGpsPoints();
        //validateGps(gps);

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

        // ✅ GPS가 없으면 여기서 그냥 끝 (심박/summary만 있는 세션)
        if (gps == null || gps.isEmpty()) {
            return 0;
        }

        // 포인트 배치 멱등 삽입
        nativeRepository.batchInsertPoints(sid, gps);

        // 라인 생성은 2개 이상이어야 의미 있으니 체크
        if (gps.size() >= 2) {
            boolean ok = nativeRepository.upsertRouteAndLength(sid, 2.0);
            if (!ok) throw new BaseException(RunResponseCode.ROUTE_BUILD_FAILED);
        }

        return sid;
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
