package com.runningcity.ui.running

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 📱 MainActivity
 * ────────────────────────────────────────────────
 * - React(WebView) ↔ Kotlin 양방향 통신
 * - GPS 권한 요청 및 ViewModel 주입 (Hilt)
 * - RunningViewModel 의 상태를 React로 실시간 전달
 * ────────────────────────────────────────────────
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** 🌐 위치 권한 목록 */
    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /** 🧠 ViewModel (Hilt 자동 주입) */
    private val viewModel: RunningViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLocationPermissions() // 위치 권한 확인 및 요청

        setContent {
            val coroutineScope = rememberCoroutineScope()
            var webView by remember { mutableStateOf<WebView?>(null) }

            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("🏙️ RunningCity") },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            ) { padding ->

                // ✅ React WebView 로드
                webView = RunningWebView(
                    url = "http://70.12.112.97:5173/",
                    viewModel = viewModel,
                    modifierPadding = padding
                )

                // 🔁 ViewModel → React 실시간 데이터 전송
                LaunchedEffect(viewModel) {
                    viewModel.uiState.collectLatest { state ->
                        // 📦 JSON 형태로 변환
                        val json = """
                            {
                              "isRunning": ${state.isRunning},
                              "distanceKm": ${state.distanceKm},
                              "durationSec": ${state.durationSec},
                              "avgPace": ${"%.2f".format(state.avgPace)}
                            }
                        """.trimIndent()

                        // JS 함수 호출 (React 수신용)
                        val jsCode = "window.receiveFromAndroid($json);"
                        coroutineScope.launch {
                            webView?.evaluateJavascript(jsCode, null)
                        }
                    }
                }
            }
        }
    }

    /**
     * 🔒 위치 권한 요청
     * ────────────────────────────────────────────────
     * - FINE(정확) + COARSE(대략적) 권한 모두 확인
     * - 미부여 시 런타임 요청
     * ────────────────────────────────────────────────
     */
    private fun checkLocationPermissions() {
        val notGranted = LOCATION_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
        }
    }
}
