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
                    
                    // JSONObject를 사용하여 안전하게 JSON 생성 (특수문자 이스케이프 처리)
                    val jsonMessage = JSONObject().apply {
                        put("type", "HEART_RATE_ERROR")
                        put("message", errorMessage)
                    }.toString()
                    sendToReact(jsonMessage)
                    
                    Log.e("WebAppInterface", "✅ React로 에러 메시지 전달 완료")
                    Log.e("WebAppInterface", "═══════════════════════════════════════")
                }
            }
        }
    }
    
    // 워치 제어 메시지 브로드캐스트 리시버
    private val watchControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.runningcity.WATCH_START_RUNNING" -> {
                    Log.d("WebAppInterface", "🏃 워치에서 러닝 시작 요청 수신")
                    sendToReact("""{"type": "WATCH_CONTROL", "action": "START", "navigate": "/running"}""")
                }
                "com.runningcity.WATCH_PAUSE_RUNNING" -> {
                    Log.d("WebAppInterface", "⏸️ 워치에서 러닝 일시정지 요청 수신")
                    sendToReact("""{"type": "WATCH_CONTROL", "action": "PAUSE"}""")
                }
                "com.runningcity.WATCH_RESUME_RUNNING" -> {
                    Log.d("WebAppInterface", "▶️ 워치에서 러닝 재개 요청 수신")
                    sendToReact("""{"type": "WATCH_CONTROL", "action": "RESUME"}""")
                }
                "com.runningcity.WATCH_STOP_RUNNING" -> {
                    Log.d("WebAppInterface", "⏹️ 워치에서 러닝 중단 요청 수신")
                    sendToReact("""{"type": "WATCH_CONTROL", "action": "STOP"}""")
                }
            }
        }
    }
    
    // 워치 결과 데이터 브로드캐스트 리시버
    private val workoutDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.runningcity.WORKOUT_DATA_RECEIVED" -> {
                    val jsonString = intent.getStringExtra("workoutData")
                    if (jsonString != null) {
                        Log.d("WebAppInterface", "📊 워치 결과 데이터 수신 (브로드캐스트) - React로 전달")
                        // JSON 문자열을 그대로 전달 (React에서 파싱)
                        sendToReact("""{"type": "WORKOUT_RESULT", "data": $jsonString}""")
                        Log.d("WebAppInterface", "✅ React로 워치 결과 데이터 전송 완료")
                        
                        // ✅ 브로드캐스트로 받은 데이터는 SharedPreferences에서 삭제 (중복 방지)
                        val prefs = context?.getSharedPreferences("watch_data", Context.MODE_PRIVATE)
                        prefs?.edit()
                            ?.remove("pending_workout_data")
                            ?.remove("pending_workout_timestamp")
                            ?.apply()
                        Log.d("WebAppInterface", "✅ SharedPreferences에서 워치 데이터 삭제 완료 (중복 방지)")
                    } else {
                        Log.e("WebAppInterface", "❌ 워치 결과 데이터가 null입니다")
                    }
                }
            }
        }
    }
    
    init {
        // 브로드캐스트 리시버 등록
        val heartRateFilter = IntentFilter().apply {
            addAction("com.runningcity.HEART_RATE_MEASURED")
            addAction("com.runningcity.HEART_RATE_ERROR")
        }
        context.registerReceiver(
            heartRateReceiver,
            heartRateFilter,
            Context.RECEIVER_NOT_EXPORTED
        )
        
        val watchControlFilter = IntentFilter().apply {
            addAction("com.runningcity.WATCH_START_RUNNING")
            addAction("com.runningcity.WATCH_PAUSE_RUNNING")
            addAction("com.runningcity.WATCH_RESUME_RUNNING")
            addAction("com.runningcity.WATCH_STOP_RUNNING")
        }
        context.registerReceiver(
            watchControlReceiver,
            watchControlFilter,
            Context.RECEIVER_NOT_EXPORTED
        )
        
        val workoutDataFilter = IntentFilter().apply {
            addAction("com.runningcity.WORKOUT_DATA_RECEIVED")
        }
        context.registerReceiver(
            workoutDataReceiver,
            workoutDataFilter,
            Context.RECEIVER_NOT_EXPORTED
        )
        
        Log.d("WebAppInterface", "✅ 심박수, 워치 제어, 워치 결과 데이터 브로드캐스트 리시버 등록 완료")
        
        // ✅ 앱이 열릴 때 보류 중인 워치 데이터 확인 및 전달
        checkPendingWorkoutData()
    }
    
    /**
     * 앱이 열릴 때 보류 중인 워치 데이터를 확인하고 React로 전달
     */
    private fun checkPendingWorkoutData() {
        try {
            val prefs = context.getSharedPreferences("watch_data", Context.MODE_PRIVATE)
            val pendingData = prefs.getString("pending_workout_data", null)
            val timestamp = prefs.getLong("pending_workout_timestamp", 0L)
            
            if (pendingData != null && timestamp > 0) {
                // 24시간 이내의 데이터만 처리 (오래된 데이터는 무시)
                val currentTime = System.currentTimeMillis()
                val timeDiff = currentTime - timestamp
                val oneDayInMillis = 24 * 60 * 60 * 1000L
                
                if (timeDiff < oneDayInMillis) {
                    Log.d("WebAppInterface", "📊 보류 중인 워치 데이터 발견 - React로 전달")
                    Log.d("WebAppInterface", "   데이터 수신 시간: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(timestamp))}")
                    Log.d("WebAppInterface", "   경과 시간: ${timeDiff / 1000}초")
                    
                    // React로 전달
                    sendToReact("""{"type": "WORKOUT_RESULT", "data": $pendingData}""")
                    Log.d("WebAppInterface", "✅ 보류 중인 워치 데이터 React로 전송 완료")
                    
                    // 전달 후 SharedPreferences에서 삭제
                    prefs.edit()
                        .remove("pending_workout_data")
                        .remove("pending_workout_timestamp")
                        .apply()
                    Log.d("WebAppInterface", "✅ 보류 중인 워치 데이터 삭제 완료")
                } else {
                    Log.d("WebAppInterface", "⚠️ 보류 중인 워치 데이터가 너무 오래됨 (24시간 초과) - 삭제")
                    prefs.edit()
                        .remove("pending_workout_data")
                        .remove("pending_workout_timestamp")
                        .apply()
                }
            } else {
                Log.d("WebAppInterface", "📭 보류 중인 워치 데이터 없음")
            }
        } catch (e: Exception) {
            Log.e("WebAppInterface", "❌ 보류 중인 워치 데이터 확인 실패: ${e.message}", e)
        }
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
            val sessionIdLong = sessionId.toLongOrNull()
            if (sessionIdLong != null) {
                // 실제 세션 시작 로직으로 전달
                viewModel.startSession(sessionIdLong)
                sendToReact("""{"type": "RUNNING_STATE", "state": "STARTED"}""")
                
                // 워치에 시작 메시지 전송
                CoroutineScope(Dispatchers.IO).launch {
                    WatchCommunicationHelper.sendStartWorkout(context, sessionIdLong)
                }
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
            val sessionIdLong = sessionId.toLongOrNull()
            if (sessionIdLong != null) {
                // 실제 세션 종료 로직으로 전달
                viewModel.stopSession(sessionIdLong)
                sendToReact("""{"type": "RUNNING_STATE", "state": "STOPPED"}""")
                
                // 워치에 중지 메시지 전송
                CoroutineScope(Dispatchers.IO).launch {
                    WatchCommunicationHelper.sendStopWorkout(context)
                }
            } else {
                Log.e("WebAppInterface", "sessionId 변환 실패: $sessionId")
            }
        } catch (e: Exception) {
            Log.e("WebAppInterface", "stopRunning() 예외 발생: ${e.message}")
        }
    }
    
    /**
     * 러닝 일시정지
     */
    @JavascriptInterface
    fun pauseRunning(sessionId: String) {
        Log.d("WebAppInterface", "⏸️ 러닝 일시정지 요청 수신 — sessionId: $sessionId")
        
        try {
            // 워치에 일시정지 메시지 전송
            CoroutineScope(Dispatchers.IO).launch {
                WatchCommunicationHelper.sendPauseWorkout(context)
            }
            sendToReact("""{"type": "RUNNING_STATE", "state": "PAUSED"}""")
        } catch (e: Exception) {
            Log.e("WebAppInterface", "pauseRunning() 예외 발생: ${e.message}")
        }
    }
    
    /**
     * 러닝 재개
     */
    @JavascriptInterface
    fun resumeRunning(sessionId: String) {
        Log.d("WebAppInterface", "▶️ 러닝 재개 요청 수신 — sessionId: $sessionId")
        
        try {
            // 워치에 재개 메시지 전송
            CoroutineScope(Dispatchers.IO).launch {
                WatchCommunicationHelper.sendResumeWorkout(context)
            }
            sendToReact("""{"type": "RUNNING_STATE", "state": "RUNNING"}""")
        } catch (e: Exception) {
            Log.e("WebAppInterface", "resumeRunning() 예외 발생: ${e.message}")
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
                // 실패 시 React에 에러 메시지 전달 (JSONObject 사용)
                val jsonMessage = JSONObject().apply {
                    put("type", "HEART_RATE_ERROR")
                    put("message", "워치 연결을 확인해주세요")
                }.toString()
                sendToReact(jsonMessage)
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
        Log.d("WebAppInterface", "📤 React로 메시지 전송: $message")
        webView.post {
            val jsCode = "if (typeof window.onAndroidMessage === 'function') { window.onAndroidMessage($message); } else { console.error('window.onAndroidMessage is not a function'); }"
            webView.evaluateJavascript(jsCode) { result ->
                Log.d("WebAppInterface", "📥 JavaScript 실행 결과: $result")
            }
        }
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
