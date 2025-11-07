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
        validateGps(req.getGpsPoints());

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

        nativeRepository.batchInsertPoints(sid, req.getGpsPoints());
        boolean ok = nativeRepository.upsertRouteAndLength(sid, 2.0);
        if (!ok) throw new BaseException(RunResponseCode.ROUTE_BUILD_FAILED);
    }


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
