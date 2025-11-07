package com.runningcity.ui.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runningcity.domain.usecase.StartSessionUseCase
import com.runningcity.domain.usecase.StopSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 🧠 RunningViewModel
 * ────────────────────────────────────────────────
 * - 러닝 세션의 전체 상태를 관리하는 중심 뷰모델
 * - Start / Stop / Metric 업데이트 로직
 * - React(WebView) ↔ Android 통신에서 상태 전달의 핵심
 * - Flow 기반으로 실시간 UI / Web 연동
 * ────────────────────────────────────────────────
 */
@HiltViewModel
class RunningViewModel @Inject constructor(
    private val startSessionUseCase: StartSessionUseCase,
    private val stopSessionUseCase: StopSessionUseCase
) : ViewModel() {

    /**
     * 💾 러닝 상태 데이터 클래스
     * - Compose or React 로 전달되는 UI 데이터 모델
     */
    data class RunningUiState(
        val isRunning: Boolean = false,  // 러닝 세션 실행 여부
        val distanceKm: Double = 0.0,    // 총 이동 거리 (km)
        val durationSec: Long = 0L,      // 총 소요 시간 (초)
        val avgPace: Double = 0.0        // 평균 페이스 (km당 분속)
    )

    /** 🔁 StateFlow: 러닝 상태를 실시간 관찰 가능한 형태로 유지 */
    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState

    // ----------------------------------------------------------------------
    // ▶️ 세션 시작
    // ----------------------------------------------------------------------
    /**
     * React → Android : `window.Android.startRunning()` 호출 시 실행
     * - 내부적으로 StartSessionUseCase 실행 (GPS 시작 등)
     * - StateFlow 값 갱신 → Compose & WebView에 반영됨
     */
    fun startSession() {
        if (_uiState.value.isRunning) {
            Log.d("RunningViewModel", "⚠️ 이미 러닝 중입니다.")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("RunningViewModel", "🏁 러닝 세션 시작")
                startSessionUseCase() // 실제 GPS 로직 실행
                _uiState.value = _uiState.value.copy(isRunning = true)
            } catch (e: Exception) {
                Log.e("RunningViewModel", "❌ 러닝 시작 실패: ${e.message}")
            }
        }
    }

    // ----------------------------------------------------------------------
    // ⏹️ 세션 중지
    // ----------------------------------------------------------------------
    /**
     * React → Android : `window.Android.stopRunning()` 호출 시 실행
     * - 내부적으로 StopSessionUseCase 실행 (GPS 중지 등)
     * - 종료 시 isRunning = false 로 상태 갱신
     */
    fun stopSession() {
        if (!_uiState.value.isRunning) {
            Log.d("RunningViewModel", "⚠️ 이미 중지 상태입니다.")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("RunningViewModel", "🛑 러닝 세션 중지")
                stopSessionUseCase()
            } catch (e: Exception) {
                Log.e("RunningViewModel", "❌ 러닝 중지 실패: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isRunning = false)
            }
        }
    }

    // ----------------------------------------------------------------------
    // 📈 거리 / 시간 / 페이스 실시간 업데이트
    // ----------------------------------------------------------------------
    /**
     * - GPS 데이터 수신 시 호출 (ex: Repository → ViewModel)
     * - distanceKm: 누적 거리
     * - durationSec: 총 경과 시간
     * - pace: 거리 대비 분속 계산 (0으로 나누기 방지)
     */
    fun updateMetrics(distanceKm: Double, durationSec: Long) {
        val pace = if (distanceKm > 0) durationSec / 60.0 / distanceKm else 0.0
        _uiState.value = _uiState.value.copy(
            distanceKm = distanceKm,
            durationSec = durationSec,
            avgPace = pace
        )

        Log.d(
            "RunningViewModel",
            "📊 거리: %.2f km | 시간: %d sec | 페이스: %.2f min/km".format(distanceKm, durationSec, pace)
        )
    }

    // ----------------------------------------------------------------------
    // 🔄 상태 토글 (디버그 or 테스트용)
    // ----------------------------------------------------------------------
    fun toggleSession() {
        if (_uiState.value.isRunning) stopSession() else startSession()
    }
}
