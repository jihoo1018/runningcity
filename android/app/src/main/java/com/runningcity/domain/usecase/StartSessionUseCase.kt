package com.runningcity.domain.usecase

import com.runningcity.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * 🚀 StartSessionUseCase
 * ────────────────────────────────────────────────
 * - 러닝 세션 시작 시 호출
 * - ForegroundService 실행 및 버퍼 초기화
 * ────────────────────────────────────────────────
 */
class StartSessionUseCase @Inject constructor(private val repository: LocationRepository) {
    suspend operator fun invoke() = repository.startSession()
}
