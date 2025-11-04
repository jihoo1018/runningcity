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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.runningcity.data.location.LocationRepositoryImpl
import com.runningcity.domain.usecase.StartSessionUseCase
import com.runningcity.domain.usecase.StopSessionUseCase
import dagger.hilt.android.AndroidEntryPoint

/**
 * 📱 MainActivity (Compose + WebView)
 * ────────────────────────────────────────────────
 * - React(WebView) ↔ Kotlin 통신
 * - GPS 권한 요청 및 ViewModel 주입
 * - WebAppInterface 연결
 * ────────────────────────────────────────────────
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** 🌐 위치 권한 목록 */
    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /** 🧠 ViewModel 자동 주입 */
    private val viewModel: RunningViewModel by viewModels() // Hilt가 자동 주입

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLocationPermissions()

        setContent { // setContent { ... } → XML 대신 화면을 직접 그리는 진입점
            Scaffold(//Scaffold() →화면 전체 레이아웃 (기본 구조: 상단바 + 본문)
                topBar = {
                    CenterAlignedTopAppBar(title = { Text("🏙️ RunningCity") })
                    //Text() → 글자 출력
                }
            ) { padding ->
                RunningWebView( //RunningWebView() → 내가 만든 WebView를 Composable 형태로 사용
                    url = "http://k13a405.p.ssafy.io/", // 🌐 React 서버 주소
                    viewModel = viewModel,
                    modifierPadding = padding
                )
            }
        }
    }

    /** 🔒 위치 권한 요청 */
    private fun checkLocationPermissions() {
        val notGranted = LOCATION_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
        }
    }
}
