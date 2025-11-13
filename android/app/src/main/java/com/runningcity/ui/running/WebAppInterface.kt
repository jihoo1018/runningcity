package com.runningcity.ui.running

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.runningcity.utils.WatchCommunicationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * WebAppInterface (React ↔ Android 브릿지)
 * ────────────────────────────────────────────────
 * - React(WebView) → Kotlin : JS 함수 호출 처리
 * - Kotlin → React(WebView) : evaluateJavascript() 로 데이터 전달
 * - RunningViewModel과 연결되어 러닝 상태 관리
 *  - window.Android.* 호출에 응답
 * ────────────────────────────────────────────────
 */

class WebAppInterface(
    private val context: Context,
    private val webView: WebView,
    private val viewModel: RunningViewModel
) {
    
    // 심박수 측정 결과 브로드캐스트 리시버
    private val heartRateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.runningcity.HEART_RATE_MEASURED" -> {
                    val heartRate = intent.getIntExtra("heartRate", 0)
                    if (heartRate > 0) {
                        Log.d("WebAppInterface", "═══════════════════════════════════════")
                        Log.d("WebAppInterface", "💓 [6단계] 브로드캐스트 수신: 심박수 측정 결과")
                        Log.d("WebAppInterface", "   📥 수신된 심박수: $heartRate bpm")
                        Log.d("WebAppInterface", "   📤 React로 전달 중...")
                        
                        val jsonMessage = """{"type": "HEART_RATE_MEASURED", "heartRate": $heartRate}"""
                        sendToReact(jsonMessage)
                        
                        Log.d("WebAppInterface", "✅ [7단계] React로 심박수 전달 완료!")
                        Log.d("WebAppInterface", "   → React에서 input 필드에 자동 입력됨")
                        Log.d("WebAppInterface", "═══════════════════════════════════════")
                    } else {
                        Log.e("WebAppInterface", "❌ 유효하지 않은 심박수 값: $heartRate")
                    }
                }
                "com.runningcity.HEART_RATE_ERROR" -> {
                    val errorMessage = intent.getStringExtra("errorMessage") ?: "심박수 측정에 실패했습니다"
                    Log.e("WebAppInterface", "═══════════════════════════════════════")
                    Log.e("WebAppInterface", "❌ [에러] 브로드캐스트 수신: 심박수 측정 에러")
                    Log.e("WebAppInterface", "   📥 에러 메시지: $errorMessage")
                    Log.e("WebAppInterface", "   📤 React로 전달 중...")
                    
                    val jsonMessage = """{"type": "HEART_RATE_ERROR", "message": "$errorMessage"}"""
                    sendToReact(jsonMessage)
                    
                    Log.e("WebAppInterface", "✅ React로 에러 메시지 전달 완료")
                    Log.e("WebAppInterface", "═══════════════════════════════════════")
                }
            }
        }
    }
    
    init {
        // 브로드캐스트 리시버 등록
        val filter = IntentFilter().apply {
            addAction("com.runningcity.HEART_RATE_MEASURED")
            addAction("com.runningcity.HEART_RATE_ERROR")
        }
        context.registerReceiver(
            heartRateReceiver,
            filter,
            Context.RECEIVER_NOT_EXPORTED
        )
        
        Log.d("WebAppInterface", "✅ 심박수 브로드캐스트 리시버 등록 완료")
    }

    /** ✅ Android → React 통신 테스트 함수 */
    fun sendMessageToReact() {
        val js = "window.receiveFromAndroid({ message: 'Hello from Android 👋' });"
        webView.post { webView.evaluateJavascript(js, null) }
        Log.d("BridgeTest", "📡 Android → React 메시지 전송 실행됨")
    }


    /**
     * 러닝 시작
     */
    @JavascriptInterface
    fun startRunning(sessionId: String) {
        Log.d("WebAppInterface", "🏃 러닝 시작 요청 수신 — sessionId: $sessionId")

        try {
            val sessionId = sessionId.toLongOrNull()
            if (sessionId != null) {
                // 실제 세션 시작 로직으로 전달
                viewModel.startSession(sessionId)
                sendToReact("""{"type": "RUNNING_STATE", "state": "STARTED"}""")
            } else {
                Log.e("WebAppInterface", "sessionId 변환 실패: $sessionId")
            }
        } catch (e: Exception) {
            Log.e("WebAppInterface", "startRunning() 예외 발생: ${e.message}")
        }

    }

    /**
     * 러닝 종료
     */
    @JavascriptInterface
    fun stopRunning(sessionId: String) {
        Log.d("BridgeTest", "✅ React → Android 통신 성공: stopRunning() 호출됨 - sessionId: $sessionId")
        try {
            val sessionId = sessionId.toLongOrNull()
            if (sessionId != null) {
                // 실제 세션 종료 로직으로 전달
                viewModel.stopSession(sessionId)
                sendToReact("""{"type": "RUNNING_STATE", "state": "STOPPED"}""")
            } else {
                Log.e("WebAppInterface", "sessionId 변환 실패: $sessionId")
            }
        } catch (e: Exception) {
            Log.e("WebAppInterface", "stopRunning() 예외 발생: ${e.message}")
        }
    }

    /**
     * GPS 데이터 가져오기
     */
    @JavascriptInterface
    fun getGPSData() {
        Log.d("BridgeTest", "React -> Android: getGPSData()")

    }

    /**
     * 토스트 메시지 표시
     */
    @JavascriptInterface
    fun showToast(msg: String) {
        Log.d("BridgeTest", "React -> Android: showToast()")
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }


    /**
     * 워치에서 심박수 측정 요청
     */
    @JavascriptInterface
    fun measureHeartRate() {
        Log.d("WebAppInterface", "═══════════════════════════════════════")
        Log.d("WebAppInterface", "💓 [1단계] React → Android: measureHeartRate() 호출됨")
        Log.d("WebAppInterface", "📤 워치에 심박수 측정 요청 전송 중...")
        
        CoroutineScope(Dispatchers.IO).launch {
            val success = WatchCommunicationHelper.requestHeartRateMeasurement(context)
            if (!success) {
                Log.e("WebAppInterface", "❌ [실패] 워치 심박수 측정 요청 실패")
                Log.e("WebAppInterface", "   → 워치가 연결되어 있는지 확인해주세요")
                // 실패 시 React에 에러 메시지 전달
                sendToReact("""{"type": "HEART_RATE_ERROR", "message": "워치 연결을 확인해주세요"}""")
            } else {
                Log.d("WebAppInterface", "✅ [2단계] 워치 심박수 측정 요청 전송 완료")
                Log.d("WebAppInterface", "   → 워치에서 측정을 시작합니다 (약 15초 소요)")
                Log.d("WebAppInterface", "   → 측정 결과를 기다리는 중...")
            }
        }
    }

    /**
     * 진동
     */
//    @JavascriptInterface
//    fun vibrate() {
//        Log.d("BridgeTest", "React -> Android: vibrate()")
//    }

    /** ✅ Android → React 메세지 전달 */
    private fun sendToReact(message: String) {
        webView.post {
            webView.evaluateJavascript(
                "window.onAndroidMessage($message)",
                null
            )
        }
//        Handler(Looper.getMainLooper()).post {
//            webView.evaluateJavascript("window.onAndroidMessage($message)", null)
//        }
    }


    /** ✅ Android → React : 상태를 JS 함수로 전달 */
//    fun sendUiStateToReact(funcName: String, json: JSONObject) {
//        // React 측에 전달될 JS 함수 (window.receiveFromAndroid 등)
//        webView.post {
//            webView.evaluateJavascript(
//                "window."+funcName+"($json)",
//                null
//            )
//        }
//    }



    /** ✅ Android → React : 상태를 JS 함수로 전달 */
//    fun sendUiStateToReact() {
//        val state = viewModel.uiState.value
//        val json = JSONObject().apply {
//            put("isRunning", state.isRunning)
//            put("distanceKm", state.distanceKm)
//            put("durationSec", state.durationSec)
//            put("avgPace", state.avgPace)
//        }.toString()
//
//        // React 측에 전달될 JS 함수 (window.receiveFromAndroid 등)
//        webView.post {
//            webView.evaluateJavascript(
//                "window.receiveFromAndroid($json)",
//                null
//            )
//        }
//    }


}
