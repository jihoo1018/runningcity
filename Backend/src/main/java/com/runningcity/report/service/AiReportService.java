package com.runningcity.report.service;

import com.runningcity.global.client.gms.OpenAiClient;
import com.runningcity.onboarding.entity.UserPreference;
import com.runningcity.onboarding.repository.UserPreferenceRepository;
import com.runningcity.report.entity.RunAiReport;
import com.runningcity.report.repository.RunAiReportRepository;
import com.runningcity.report.repository.RunSessionRepository;
import com.runningcity.run.dto.FinishRequest;
import com.runningcity.run.entity.RunSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReportService {
    private final OpenAiClient aiClient;
    private final UserPreferenceRepository preferenceRepository;
    private final RunAiReportRepository aiReportRepository;
    private final RunSessionRepository runSessionRepository;

    private static final String MODEL = "gpt-4.1-mini";

    private static final String SYSTEM_PROMPT = """
            You are a professional running coach.
            You analyze running session data and provide brief feedback in Korean.
            
            Always follow these rules:
            - 답변은 반드시 한국어로 작성한다.
            - 전체 분량은 400자 이내로 유지한다.
            - 3~5문장으로 작성한다.
            - 첫 문장은 오늘 달리기에 대한 총평이다.
            - 중간 문장에서는 세션의 특징(페이스, 심박, 거리 등)을 기반으로 좋았던 점과 개선할 점을 균형 있게 설명한다.
            - 마지막 문장은 사용자의 피트니스 수준과 목표 거리를 반영한 조언으로 마무리한다.
            - 숫자는 러닝에서 익숙한 단위(km, 분/초 페이스, bpm 등)를 사용한다.
            - 피트니스 레벨(BEGINNER → INTERMEDIATE → ADVANCED → EXPERT → ELITE)은 제공된 정보에 맞게 고려하되, 레벨 이름을 그대로 언급하지 않는다.(초보자, 중급자, 상급자 등으로 대체)
            - 피트니스 수준이 낮을수록 이해하기 쉬운 설명을, 높을수록 더 구체적이고 전문적인 수치 기반 조언을 제공한다.
            - 달리기 능력 향상에 도움이 되는 조언을 제공하되, 유저 달리기 수준에 비해 과도하게 높은 강도를 요구하는 조언은 피하고 안전과 건강을 우선한다.
            """;

    /**
     * 세션 종료 시 바로 호출되는 AI 리포트 생성
     * 실패시 null 반환, DB 저장 X
     * */
    @Transactional
    public String generateAndSave(long userId, long sessionId, FinishRequest req) {
        UserPreference pref = preferenceRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalStateException("유저 온보딩 정보를 찾을 수 없습니다."));

        FinishRequest.Summary s = req.getSummary();

        double distanceKm = (s.getTotalDistance() != null ? s.getTotalDistance() : 0) / 1000.0;
        int durationSec   = s.getDuration() != null ? s.getDuration() : 0;
        int avgPace       = s.getAvgPace() != null ? s.getAvgPace() : 0;
        Integer avgHr     = s.getAvgHeartRate();
        double elevation  = s.getElevation() != null ? s.getElevation() : 0.0;

        String userPrompt = """
            세션 정보:
            distance_km=%.2f
            duration_sec=%d
            pace_sec_per_km=%d
            avg_heart_rate=%s
            elevation_gain_m=%.1f

            사용자 정보:
            fitness_level=%s
            target_distance_km=%.1f
            resting_heart_rate=%s

            위 정보를 바탕으로 SYSTEM 메세지에 적힌 형식과 규칙을 그대로 따르며,
            오늘 달리기 평가, 세션 요약 및 개선/장점, 그리고 피트니스 레벨/목표 기반 조언을 3~5문장으로 작성해 주세요.
            target_distance_km는 사용자의 1회 달리기 완주 목표 거리입니다.
            평균 심박수와 달리기 세션의 평균 심박수를 고려하여 적절한 운동 강도인지에 대한 조언을 제공하세요.
            """
                .formatted(
                        distanceKm,
                        durationSec,
                        avgPace,
                        (avgHr != null ? avgHr + " bpm" : "정보 없음"),
                        elevation,
                        pref.getFitnessLevel(),
                        pref.getTargetDistanceKm(),
                        (pref.getRestingHeartRate() != null ? pref.getRestingHeartRate()+" bpm" : "정보 없음")
                );

        String content = null;

        try {
            content = aiClient.chat(MODEL, SYSTEM_PROMPT, userPrompt);
        } catch (Exception e) {
            log.error("[AI REPORT] OpenAI 호출 실패. userId={}, sessionId={}", userId, sessionId, e);
            return null;
        }

        if (content == null || content.isBlank()) {
            log.warn("[AI REPORT] 빈 응답. userId={}, sessionId={}", userId, sessionId);
            return null;
        }

        RunSession session = runSessionRepository.getReferenceById(sessionId);

        aiReportRepository.save(
                RunAiReport.builder()
                        .runSession(session)
                        .model(MODEL)
                        .content(content)
                        .build()
        );

        return content;
    }

    /**
     * 재요청 API용
     * FinishRequest 없이 RunSession + UserPreference 기반으로 생성
     */
    @Transactional
    public String generateFromSession(long userId, long sessionId) {
        UserPreference pref = preferenceRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalStateException("유저 온보딩 정보를 찾을 수 없습니다."));

        RunSession session = runSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("RunSession을 찾을 수 없습니다."));

        double distanceKm = session.getTotalDistance() != null ? session.getTotalDistance() / 1000.0 : 0.0;
        int durationSec   = session.getDuration() != null ? session.getDuration() : 0;
        int avgPace       = session.getAvgPace() != null ? session.getAvgPace() : 0;
        Integer avgHr     = session.getAvgHeartRate();
        double elevation  = session.getElevation() != null ? session.getElevation() : 0.0;

        String userPrompt = """
            세션 정보:
            distance_km=%.2f
            duration_sec=%d
            pace_sec_per_km=%d
            avg_heart_rate=%s
            elevation_gain_m=%.1f

            사용자 정보:
            fitness_level=%s
            monthly_target_distance_km=%.1f
            resting_heart_rate=%s

            위 정보를 바탕으로 SYSTEM 메세지에 적힌 형식과 규칙을 그대로 따르며,
            오늘 달리기 평가, 세션 요약 및 개선/장점, 그리고 피트니스 레벨/목표 기반 조언을 3~5문장으로 작성해 주세요.
            """
                .formatted(
                        distanceKm,
                        durationSec,
                        avgPace,
                        (avgHr != null ? avgHr + " bpm" : "정보 없음"),
                        elevation,
                        pref.getFitnessLevel(),
                        pref.getTargetDistanceKm(),
                        (pref.getRestingHeartRate() != null ? pref.getRestingHeartRate()+" bpm" : "정보 없음")
                );

        String content;
        try {
            content = aiClient.chat(MODEL, SYSTEM_PROMPT, userPrompt);
        } catch (Exception e) {
            log.error("[AI REPORT] 재생성 실패. userId={}, sessionId={}", userId, sessionId, e);
            return null;
        }

        if (content == null || content.isBlank()) {
            log.warn("[AI REPORT] 재생성 응답이 비어있음. userId={}, sessionId={}", userId, sessionId);
            return null;
        }

        aiReportRepository.save(
                RunAiReport.builder()
                        .runSession(session)
                        .model(MODEL)
                        .content(content)
                        .build()
        );

        return content;
    }
}
