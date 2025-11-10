package com.runningcity

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.runningcity.data.WorkoutDataBatch
import com.runningcity.ui.theme.RunningcityTheme
import com.runningcity.utils.WatchCommunicationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * MainActivity
 * - 모바일에서 운동을 시작하는 메인 화면
 * - 시작 버튼을 누르면 세션 ID를 생성하고 워치에 전달
 */
class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 2단계에서 만든 레이아웃 설정
        setContent {
            RunningcityTheme {
                // Surface가 화면 전체를 채웁니다.
                Surface(
                    modifier = Modifier.fillMaxSize(), // 👈 1단계: Surface가 전체 화면을 채우도록 함
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 웹뷰 컴포저블을 호출합니다.
                    FullSizeWebView(url = BuildConfig.WEBVIEW_URL)
                    
                    // 워치 통신 라이프사이클 유지
                    WatchCommunicationLifecycle()
                }
            }
        }
    }

    /**
     * 🌐 전체 화면을 차지하는 WebView를 표시하는 Composable
     * @param url 로드할 웹사이트 주소
     */
    @Composable
    fun FullSizeWebView(url: String) {
        val context = LocalContext.current

        AndroidView(
            // 🌟 이 Modifier가 화면 전체를 꽉 채우는 핵심입니다 🌟
            modifier = Modifier.fillMaxSize(), 
            factory = {
                // WebView 객체를 생성하고 초기 설정
                WebView(context).apply {
                    // 1. 웹뷰 클라이언트 및 크롬 클라이언트 설정
                    this.webViewClient = WebViewClient()
                    this.webChromeClient = WebChromeClient()
                    
                    // 2. 웹뷰 설정 (JavaScript, Viewport, Cache 등)
                    this.settings.apply {
                        javaScriptEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        domStorageEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT 
                    }
                    
                    // 3. React 웹사이트 주소 로드!
                    loadUrl(url)
                }
            },
            // URL이 변경될 때만 새로 로드
            update = { webView ->
                if (webView.url != url) {
                    webView.loadUrl(url)
                }
            }
        )
    }
    
    /* // 워치 통신 부분 일단 지워둠
    override fun onResume() {
        super.onResume()
        
        // 모바일 앱이 열렸을 때 워치에 준비 완료 알림
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            WatchCommunicationHelper.sendMobileReady(this@MainActivity)
        }
    }
    */
}

/**
 * ⌚ 워치 통신 시작 및 수신 로직을 관리하는 Composable
 * - MobileReady 전송 (onResume 역할) 및 Broadcast Receiver 등록/해제를 담당합니다.
 */
@Composable
fun WatchCommunicationLifecycle() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gson = remember { Gson() }
    
    // 이 상태 변수들은 데이터 로깅이나 UI가 없으므로 필요 없으나, 
    // Receiver 내부 로직을 위해 isForeground 상태만 임시로 유지합니다.
    var isForeground by remember { mutableStateOf(true) }

    // Component 라이프사이클에 맞춰 Broadcast Receiver를 등록하고 해제합니다.
    DisposableEffect(Unit) {
        // 1. (onResume 역할) 모바일 앱이 열렸을 때 워치에 준비 완료 알림
        isForeground = true
        CoroutineScope(Dispatchers.IO).launch {
            WatchCommunicationHelper.sendMobileReady(context)
        }
        
        // 2. Broadcast Receiver 등록 (워치로부터 메시지 수신)
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                when (intent?.action) {
                    "com.runningcity.WATCH_DATA_READY" -> {
                        if (isForeground) {
                            scope.launch {
                                WatchCommunicationHelper.sendMobileReady(context)
                            }
                        }
                    }
                    // 운동 종료 수신 로직 (워치 통신 디버깅용)
                    "com.runningcity.WORKOUT_STOPPED_FROM_WATCH" -> {
                        val sessionId = intent.getLongExtra("sessionId", 0L)
                        println("⏹️ 워치에서 운동 종료됨 (sessionId: $sessionId)")
                    }
                    // 운동 데이터 수신 로직 (워치 통신 디버깅용)
                    "com.runningcity.WORKOUT_DATA_RECEIVED" -> {
                        val jsonString = intent.getStringExtra("workoutData")
                        if (jsonString != null) {
                            try {
                                val workoutData = gson.fromJson(jsonString, WorkoutDataBatch::class.java)
                                println("✅ 운동 데이터 수신 완료: ${workoutData.sessionId} (${workoutData.heartRateRecords.size}개)")
                                // TODO: 여기서 백엔드로 데이터 전송 로직을 구현합니다.
                            } catch (e: Exception) {
                                println("❌ 운동 데이터 파싱 실패: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
        
        val filter = android.content.IntentFilter().apply {
            addAction("com.runningcity.WATCH_DATA_READY")
            addAction("com.runningcity.WORKOUT_STOPPED_FROM_WATCH")
            addAction("com.runningcity.WORKOUT_DATA_RECEIVED")
        }
        
        // Broadcast Receiver 등록
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        // 컴포저블이 화면에서 사라질 때 (onPause 역할)
        onDispose {
            isForeground = false
            context.unregisterReceiver(receiver)
        }
    }
}
