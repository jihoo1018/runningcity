package com.runningcity.domain.usecase

import com.runningcity.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * 🏁 StopSessionUseCase
 * ────────────────────────────────────────────────
 * - 러닝 세션 종료 시 실행되는 UseCase
 * - LocationService 중지 + 서버로 경로 업로드
 * ────────────────────────────────────────────────
 */
class StopSessionUseCase @Inject constructor(private val repository: LocationRepository) {
    suspend operator fun invoke() = repository.stopSession()
}
