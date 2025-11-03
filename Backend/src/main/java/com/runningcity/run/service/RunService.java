package com.runningcity.run.service;

import com.runningcity.global.exception.BaseException;
import com.runningcity.global.response.CommonResponseCode;
import com.runningcity.run.dto.CreateSessionRequest;
import com.runningcity.run.dto.CreateSessionResponse;
import com.runningcity.run.dto.FinishRequest;
import com.runningcity.run.dto.FinishResponse;
import com.runningcity.run.dto.UploadPointsRequest;
import com.runningcity.run.dto.UploadPointsResponse;
import com.runningcity.run.entity.RunSession;
import com.runningcity.run.exception.RunResponseCode;
import com.runningcity.run.repository.RunNativeRepository;
import com.runningcity.run.repository.RunSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RunService {

    private final RunSessionRepository sessionRepository;
    private final RunNativeRepository nativeRepository;

    /** 세션 생성 */
    @Transactional
    public CreateSessionResponse createSession(long userId, CreateSessionRequest req) {
        RunSession session = RunSession.builder()
                .userId(userId)
                .type(req.getType())
                .deviceType(req.getDeviceType())
                .baseId(req.getBaseId())
                .startAt(Instant.now())
                .status("ACTIVE")
                .build();

        session = sessionRepository.save(session);
        nativeRepository.insertSessionUploadRow(session.getSessionId());

        return CreateSessionResponse.builder()
                .sessionId(session.getSessionId())
                .startAt(session.getStartAt())
                .status(session.getStatus())
                .build();
    }

    /** 포인트 배치 업로드 (멱등 + 연속 ACK) */
    @Transactional
    public UploadPointsResponse uploadPoints(long userId, long sid, UploadPointsRequest req) {

        RunSession session = sessionRepository.findById(sid)
                .orElseThrow(() -> new BaseException(RunResponseCode.SESSION_NOT_FOUND));

        // 세션 소유자 검증
        if (!Objects.equals(session.getUserId(), userId)) {
            throw new BaseException(CommonResponseCode.FORBIDDEN);
        }

        // FINALIZED 차단 (ACTIVE가 아니면 업로드 불가)
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new BaseException(RunResponseCode.SESSION_FINALIZED);
        }

        // 요청 바디 검증
        if (req == null || req.getPoints() == null || req.getPoints().isEmpty()) {
            throw new BaseException(RunResponseCode.INVALID_POINTS_PAYLOAD);
        }

        // 네이티브 일괄 insert
        List<Map<String, Object>> params = new ArrayList<>(req.getPoints().size());
        int batchMaxSeq = 0;
        for (UploadPointsRequest.Point p : req.getPoints()) {
            batchMaxSeq = Math.max(batchMaxSeq, p.getSeq());
            Map<String, Object> m = new HashMap<>();
            m.put("seq", p.getSeq());
            m.put("ts", p.getRecordedAt());
            m.put("lon", p.getLon());
            m.put("lat", p.getLat());
            m.put("alt", p.getAlt());
            m.put("spd", p.getSpeedMps());
            m.put("hr", p.getHrBpm());
            m.put("cad", p.getCadenceSpm());
            m.put("src", p.getSource());
            params.add(m);
        }

        // (선택) 업로드 상태 행 보장: 없으면 생성
        nativeRepository.insertSessionUploadRow(sid);

        nativeRepository.batchInsertPoints(sid, params);

        Integer ackedUntil = nativeRepository.updateAckAndReturn(sid, batchMaxSeq);
        return UploadPointsResponse.builder()
                .ackedUntilSeq(ackedUntil != null ? ackedUntil : 0)
                .maxInsertedSeq(batchMaxSeq)
                .build();
    }



    /** Finish (멱등, deadline 지나면 현재까지로 확정) */
    @Transactional
    public FinishResponse finish(long userId, long sid, FinishRequest req) {
        RunSession session = sessionRepository.findById(sid)
                .orElseThrow(() -> new BaseException(RunResponseCode.SESSION_NOT_FOUND));

        if (!Objects.equals(session.getUserId(), userId)) {
            throw new BaseException(CommonResponseCode.FORBIDDEN);
        }

        // 멱등: FINALIZED면 그대로 반환
        if ("FINALIZED".equals(session.getStatus())) {
            return FinishResponse.builder()
                    .status("FINALIZED")
                    .finalizedAt(session.getEndAt())
                    .build();
        }

        // tailPoints 선 반영
        if (req.getTailPoints() != null && !req.getTailPoints().isEmpty()) {
            List<Map<String, Object>> params = new ArrayList<>(req.getTailPoints().size());
            for (FinishRequest.TailPoint tp : req.getTailPoints()) {
                Map<String, Object> m = new HashMap<>();
                m.put("seq", tp.getSeq());
                m.put("ts", tp.getRecordedAt());
                m.put("lon", tp.getLon());
                m.put("lat", tp.getLat());
                m.put("alt", tp.getAlt());
                m.put("spd", tp.getSpeedMps());
                m.put("hr", tp.getHrBpm());
                m.put("cad", tp.getCadenceSpm());
                m.put("src", tp.getSource());
                params.add(m);
            }
            nativeRepository.batchInsertPoints(sid, params);
        }

        FinishRequest.DeviceSummary dsPatch = req.getDeviceSummary();
        if (dsPatch != null) {
            Map<String, Object> patchSummary = new HashMap<>();
            patchSummary.put("durationSec", dsPatch.getDurationSec());
            patchSummary.put("distanceKm", dsPatch.getDistanceKm() == null ? null : BigDecimal.valueOf(dsPatch.getDistanceKm()));
            patchSummary.put("avgPaceSecPerKm", dsPatch.getAvgPaceSecPerKm());
            patchSummary.put("caloriesKcal", dsPatch.getCaloriesKcal());
            patchSummary.put("elevationGainM", dsPatch.getElevationGainM());
            patchSummary.put("avgHrBpm", dsPatch.getAvgHrBpm());
            patchSummary.put("avgCadenceSpm", dsPatch.getAvgCadenceSpm());
            nativeRepository.upsertSummaryWhileOpen(sid, patchSummary); // [ADDED]
        }

        // ACK 확인
        Integer ack = nativeRepository.getAck(sid);
        int clientLast = req.getClientLastSeq();

        // 1) 충분히 받았으면 즉시 마감
        if (ack != null && ack >= clientLast) {
            try {
                nativeRepository.upsertRouteAndLength(sid, 5.0);
            } catch (Exception ignore) {}

            FinishRequest.DeviceSummary ds = req.getDeviceSummary();
            Map<String, Object> summary = new HashMap<>();
            summary.put("durationSec", ds.getDurationSec());
            summary.put("distanceKm", ds.getDistanceKm() == null ? null : BigDecimal.valueOf(ds.getDistanceKm()));
            summary.put("avgPaceSecPerKm", ds.getAvgPaceSecPerKm());
            summary.put("caloriesKcal", ds.getCaloriesKcal());
            summary.put("elevationGainM", ds.getElevationGainM());
            summary.put("avgHrBpm", ds.getAvgHrBpm());
            summary.put("avgCadenceSpm", ds.getAvgCadenceSpm());


            nativeRepository.finalizeSession(sid, summary);

            // 여기에서 리워드가 있으면 json형태로 추가하면 됨

            Instant finalizedAt = nativeRepository.getFinalizedAtOrNull(sid);
            session.setStatus("FINALIZED"); // 엔티티 캐시 정합

            return FinishResponse.builder()
                    .status("FINALIZED")
                    .finalizedAt(finalizedAt)
                    .build();
        }

        // 2) 아직 부족 → deadline 정책 적용
        boolean deadlinePassed =
                "CLOSING".equals(session.getStatus()) &&
                        session.getClosingDeadline() != null &&
                        Instant.now().isAfter(session.getClosingDeadline());

        if (deadlinePassed) {
            // 현재까지로 부분 확정
            try {
                nativeRepository.upsertRouteAndLength(sid, 5.0);
            } catch (Exception ignore) {}

            FinishRequest.DeviceSummary ds = req.getDeviceSummary();
            Map<String, Object> summary = new HashMap<>();
            summary.put("durationSec", ds.getDurationSec());
            summary.put("distanceKm", ds.getDistanceKm() == null ? null : BigDecimal.valueOf(ds.getDistanceKm()));
            summary.put("avgPaceSecPerKm", ds.getAvgPaceSecPerKm());
            summary.put("caloriesKcal", ds.getCaloriesKcal());
            summary.put("elevationGainM", ds.getElevationGainM());
            summary.put("avgHrBpm", ds.getAvgHrBpm());
            summary.put("avgCadenceSpm", ds.getAvgCadenceSpm());

            nativeRepository.finalizeSession(sid, summary);

            // 리워드 추가하면됨 (부분 확정이더라도 FINALIZED이면 저장)

            Instant finalizedAt = nativeRepository.getFinalizedAtOrNull(sid);
            session.setStatus("FINALIZED");

            return FinishResponse.builder()
                    .status("FINALIZED")
                    .finalizedAt(finalizedAt)
                    .build();
        }

        // 3) ACTIVE면 최초 한 번만 CLOSING 전환(연장 금지)
        if ("ACTIVE".equals(session.getStatus())) {
            nativeRepository.setClosingIfFirstTime(sid, 60);
            session.setStatus("CLOSING");
            session.setClosingDeadline(Instant.now().plusSeconds(60)); // 응답 힌트용
        }

        // CLOSING 계속 유지(연장 없음)
        return FinishResponse.builder()
                .status("CLOSING")
                .closingDeadline(session.getClosingDeadline())
                .serverAckedUntil(ack == null ? 0 : ack)
                .build();
    }
}