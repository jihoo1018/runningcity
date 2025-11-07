package com.runningcity.ui.running

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
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
    fun startRunning() {
        Log.d("BridgeTest", "✅ React → Android 통신 성공: startRunning() 호출됨")
        viewModel.startSession()
        sendToReact("""{"type": "RUNNING_STATE", "state": "STARTED"}""")
    }

    /**
     * 러닝 종료
     */
    @JavascriptInterface
    fun stopRunning() {
        Log.d("BridgeTest", "✅ React → Android 통신 성공: stopRunning() 호출됨")
        viewModel.stopSession()
        sendToReact("""{"type": "RUNNING_STATE", "state": "STOPPED"}""")
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
     * 심박수 가져오기
     */
//    @JavascriptInterface
//    fun getHeartRate() {
//        Log.d("BridgeTest", "React -> Android: getHeartRate()")
//
//    }

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
