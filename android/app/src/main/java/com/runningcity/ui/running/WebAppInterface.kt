package com.runningcity.ui.running

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import android.util.Log
import org.json.JSONObject

/**
 * 🤖 AndroidBridge
 * ────────────────────────────────────────────────
 * - React(WebView) ↔ Kotlin 통신 브릿지 클래스
 * - window.Android.startRunning(), stopRunning(), getRunningState() 등 가능
 * ────────────────────────────────────────────────
 */
class WebAppInterface(
    private val context: Context,
    private val webView: WebView,
    private val viewModel: RunningViewModel
) {

    /** ▶️ React → Android : 러닝 시작 */
    @JavascriptInterface
    fun startRunning() {
        Log.d("WebAppInterface", "🏃 러닝 시작 요청 수신")
        viewModel.startSession()
        sendUiStateToReact()
    }

    /** ⏹️ React → Android : 러닝 중지 */
    @JavascriptInterface
    fun stopRunning() {
        Log.d("WebAppInterface", "🛑 러닝 중지 요청 수신")
        viewModel.stopSession()
        sendUiStateToReact()
    }

    /** 🔁 React → Android : 현재 러닝 상태 요청 */
    @JavascriptInterface
    fun getRunningStatus(): String {
        val state = viewModel.uiState.value
        val json = JSONObject().apply {
            put("isRunning", state.isRunning)
            put("distanceKm", state.distanceKm)
            put("durationSec", state.durationSec)
            put("avgPace", state.avgPace)
        }.toString()

        Log.d("WebAppInterface", "📤 상태 요청 응답: $json")
        return json
    }

    /** 📡 React → Android : 데이터 전달용 (예: 위치, 하트레이트 등) */
    @JavascriptInterface
    fun sendData(data: String) {
        Log.d("WebAppInterface", "React로부터 데이터 수신: $data")
        // 필요 시 JSON 파싱 후 내부 상태 업데이트
    }

    /** ✅ Kotlin → React : 상태 갱신 push */
    fun sendUiStateToReact() {
        val state = viewModel.uiState.value
        val json = JSONObject().apply {
            put("isRunning", state.isRunning)
            put("distanceKm", state.distanceKm)
            put("durationSec", state.durationSec)
            put("avgPace", state.avgPace)
        }.toString()

        webView.post {
            webView.evaluateJavascript(
                "window.AndroidBridge.onRunningStateUpdated($json)",
                null
            )
        }
    }

    /** 🧭 React → Android : GPS 요청 */
    @JavascriptInterface
    fun requestLocation() {
        val lat = 37.5665
        val lon = 126.9780

        webView.post {
            webView.evaluateJavascript(
                "window.AndroidBridge.onLocationReceived($lat, $lon)",
                null
            )
        }
    }

    /** 💬 React → Android : 토스트 표시 */
    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
