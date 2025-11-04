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
 * - 러닝 세션의 전체 상태를 관리
 * - Start/Stop/Update 로직
 * - React(WebView) ↔ Android 통신에 활용
 * ────────────────────────────────────────────────
 */
@HiltViewModel
class RunningViewModel @Inject constructor(
    private val startSessionUseCase: StartSessionUseCase,
    private val stopSessionUseCase: StopSessionUseCase
) : ViewModel() {

    /** 🩵 러닝 상태를 담는 UI 데이터 클래스 */
    data class RunningUiState(
        val isRunning: Boolean = false,
        val distanceKm: Double = 0.0,
        val durationSec: Long = 0L,
        val avgPace: Double = 0.0  // km당 분속
    )

    /** 🔁 StateFlow로 상태 관리 */
    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState

    /**
     * ▶️ 러닝 시작
     * - React에서 window.Android.startRunning() 호출 시 실행
     */
    fun startSession() {
        if (_uiState.value.isRunning) {
            Log.d("RunningViewModel", "이미 러닝 중입니다.")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("RunningViewModel", "🏁 러닝 세션 시작")
                startSessionUseCase()
                _uiState.value = _uiState.value.copy(isRunning = true)
            } catch (e: Exception) {
                Log.e("RunningViewModel", "러닝 시작 실패: ${e.message}")
            }
        }
    }

    /**
     * ⏹️ 러닝 중지
     * - React에서 window.Android.stopRunning() 호출 시 실행
     */
    fun stopSession() {
        if (!_uiState.value.isRunning) {
            Log.d("RunningViewModel", "러닝이 이미 중지된 상태입니다.")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("RunningViewModel", "🛑 러닝 세션 중지")
                stopSessionUseCase()
            } catch (e: Exception) {
                Log.e("RunningViewModel", "러닝 중지 실패: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isRunning = false)
            }
        }
    }

    /**
     * 📈 러닝 데이터 업데이트 (거리, 시간 등)
     * - GPS 업데이트 주기마다 호출 가능
     */
    fun updateMetrics(distanceKm: Double, durationSec: Long) {
        val pace = if (distanceKm > 0) durationSec / 60.0 / distanceKm else 0.0
        _uiState.value = _uiState.value.copy(
            distanceKm = distanceKm,
            durationSec = durationSec,
            avgPace = pace
        )
    }

    /**
     * 🔄 상태 토글 (디버그용)
     */
    fun toggleSession() {
        if (_uiState.value.isRunning) stopSession() else startSession()
    }
}
