package com.runningcity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.runningcity.ui.theme.RunningcityTheme

/**
 * MainActivity
 * - 앱의 메인 화면(Activity)
 * - GPS 전송 버튼 UI를 표시하고,
 *   버튼을 눌렀을 때 LocationService를 실행하거나 중지함
 */
class MainActivity : ComponentActivity() {

    private var permissionGranted = false // 위치 권한 여부 저장용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 요청할 권한 목록
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // 권한 요청 처리
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                permissionGranted = result.values.all { it }
            }

        // 현재 권한이 이미 있는지 확인
        permissionGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        // Compose UI 표시
        setContent {
            RunningcityTheme {
                GPSControlScreen(
                    isPermissionGranted = permissionGranted,
                    onRequestPermission = { requestPermissionLauncher.launch(permissions) },
                    onStartService = { startLocationService() },
                    onStopService = { stopLocationService() }
                )
            }
        }
    }

    /**
     * ✅ 위치 서비스(LocationService) 시작
     * - 백그라운드에서 GPS 데이터를 주기적으로 전송함
     */
    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    /**
     * ✅ 위치 서비스 중지
     */
    private fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
    }
}

/**
 * GPSControlScreen
 * - UI 화면을 구성하는 Composable 함수
 * - GPS 전송 버튼, 현재 위도/경도 표시
 */
@Composable
fun GPSControlScreen(
    isPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    var isSending by remember { mutableStateOf(false) }  // 현재 GPS 전송 중 여부
    var latitude by remember { mutableStateOf<Double?>(null) }   // 현재 위도
    var longitude by remember { mutableStateOf<Double?>(null) }  // 현재 경도

    val context = LocalContext.current

    /**
     * 📡 BroadcastReceiver 등록
     * - LocationService가 보낸 "LOCATION_UPDATE" 액션을 수신함
     */
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == "LOCATION_UPDATE") {
                    latitude = intent.getDoubleExtra("latitude", 0.0)
                    longitude = intent.getDoubleExtra("longitude", 0.0)
                }
            }
        }

        val filter = IntentFilter("LOCATION_UPDATE")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0 (API 26) 이상에서는 REVEIVER_NOT_EXPORTED 플래그 필요
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            // 하위 버전에서는 flag 없이 사용
            context.registerReceiver(receiver, filter)
        }

        // 화면이 사라질 때 리시버 해제
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // UI 구성
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GPS 전송 제어",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 버튼
        Button(
            onClick = {
                if (isPermissionGranted) {
                    if (!isSending) onStartService() else onStopService()
                    isSending = !isSending
                } else {
                    onRequestPermission()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSending) "GPS 전송 중..." else "GPS 전송 시작")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 현재 위치 표시
        Text(
            text = if (latitude != null && longitude != null)
                "위도: %.6f\n경도: %.6f".format(latitude, longitude)
            else
                "현재 위치를 가져오는 중...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
