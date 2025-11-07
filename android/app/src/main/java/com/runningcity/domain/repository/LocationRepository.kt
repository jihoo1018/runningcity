package com.runningcity.domain.repository

/**
 * 📡 LocationRepository (Repository Interface)
 * ────────────────────────────────────────────────
 * - 데이터 접근 계층의 추상화.
 * - ViewModel은 서비스나 네트워크의 세부 구현을 알 필요 없이,
 *   Repository의 메서드만 호출하면 됨.
 * ────────────────────────────────────────────────
 */
interface LocationRepository {

    /** 🟢 세션 시작 (ForegroundService 실행) */
    suspend fun startSession()

    /** 🔴 세션 종료 (서비스 중단 + 서버 업로드) */
    suspend fun stopSession()
}
