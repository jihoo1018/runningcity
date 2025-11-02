package com.runningcity.presentation

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.runningcity.utils.PermissionManager

class RunningActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // PermissionManager 초기화
        permissionManager = PermissionManager(this)

        setContent {
            MaterialTheme {
                TestPermissionScreen()
            }
        }
    }

    @Composable
    fun TestPermissionScreen() {
        // 권한 상태
        var hasPermissions by remember {
            mutableStateOf(permissionManager.hasAllPermissions())
        }

        // 센서 하드웨어 확인
        var hasSensor by remember { mutableStateOf(false) }

        // 센서 상태
        val sensorStatus = permissionManager.getSensorStatus()

        // 디버그 정보
        var debugInfo by remember { mutableStateOf("") }

        // 센서 하드웨어 체크
        LaunchedEffect(Unit) {
            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            val heartRateSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_HEART_RATE)
            hasSensor = heartRateSensor != null

            debugInfo = "센서: ${if (hasSensor) "있음" else "없음"}\n" +
                    "권한: ${if (hasPermissions) "완료" else "필요"}\n" +
                    "심박수: ${if (sensorStatus.heartRate) "OK" else "X"}\n" +
                    "GPS: ${if (sensorStatus.gps) "OK" else "X"}\n" +
                    "걸음수: ${if (sensorStatus.steps) "OK" else "X"}"
        }

        Scaffold(
            timeText = { TimeText() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "권한 테스트",
                    style = MaterialTheme.typography.title2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 전체 상태
                Text(
                    text = if (hasPermissions) "✅ 준비 완료" else "❌ 권한 필요",
                    style = MaterialTheme.typography.body1,
                    color = if (hasPermissions) Color.Green else Color.Red
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 센서별 상태
                Text(
                    text = "❤️ 심박수: ${if (sensorStatus.heartRate) "OK" else "X"}",
                    style = MaterialTheme.typography.caption1
                )

                if (!hasSensor) {
                    Text(
                        text = "⚠️ 센서 없음",
                        style = MaterialTheme.typography.caption2,
                        color = Color.Yellow
                    )
                }

                Text(
                    text = "📍 GPS: ${if (sensorStatus.gps) "OK" else "X"}",
                    style = MaterialTheme.typography.caption1
                )

                Text(
                    text = "🏃 걸음수: ${if (sensorStatus.steps) "OK" else "X"}",
                    style = MaterialTheme.typography.caption1
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 버튼들
                if (!hasPermissions) {
                    // 권한 요청 버튼
                    Button(
                        onClick = {
                            permissionManager.requestPermissions { granted ->
                                hasPermissions = granted

                                // 상태 재확인
                                val newStatus = permissionManager.getSensorStatus()
                                debugInfo = "요청 완료!\n" +
                                        "심박수: ${if (newStatus.heartRate) "OK" else "X"}\n" +
                                        "GPS: ${if (newStatus.gps) "OK" else "X"}\n" +
                                        "걸음수: ${if (newStatus.steps) "OK" else "X"}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("권한 허용")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 설정 화면 버튼
                    Button(
                        onClick = {
                            startActivity(permissionManager.createSettingsIntent())
                        },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.secondaryButtonColors()
                    ) {
                        Text("설정 열기")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 누락된 권한 표시
                    val missing = permissionManager.getMissingPermissions()
                    if (missing.isNotEmpty()) {
                        Text(
                            text = "필요: ${missing.size}개",
                            style = MaterialTheme.typography.caption2
                        )
                        missing.forEach { permission ->
                            Text(
                                text = "- ${permissionManager.getPermissionDisplayName(permission)}",
                                style = MaterialTheme.typography.caption2
                            )
                        }
                    }
                } else {
                    // 성공 메시지
                    Text(
                        text = "🎉",
                        style = MaterialTheme.typography.display1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 재확인 버튼 (디버그용)
                    Button(
                        onClick = {
                            hasPermissions = permissionManager.hasAllPermissions()
                            val newStatus = permissionManager.getSensorStatus()
                            debugInfo = "재확인 완료!\n" +
                                    "심박수: ${if (newStatus.heartRate) "OK" else "X"}\n" +
                                    "GPS: ${if (newStatus.gps) "OK" else "X"}\n" +
                                    "걸음수: ${if (newStatus.steps) "OK" else "X"}"
                        },
                        colors = ButtonDefaults.secondaryButtonColors()
                    ) {
                        Text("재확인")
                    }
                }

                // 디버그 정보 (화면 하단)
                if (debugInfo.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = debugInfo,
                        style = MaterialTheme.typography.caption2,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}