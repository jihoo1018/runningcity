package com.runningcity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var permissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 요청할 권한 목록
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // 권한 요청 런처
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                permissionGranted = result.values.all { it }
            }

        // 기존 권한 상태 확인
        permissionGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        setContent {
            var isSending by remember { mutableStateOf(false) }

            GPSControlScreen(
                isSending = isSending,
                onStartClick = {
                    if (permissionGranted) {
                        startLocationService()
                        isSending = true
                    } else {
                        requestPermissionLauncher.launch(permissions)
                    }
                },
                onStopClick = {
                    stopLocationService()
                    isSending = false
                }
            )
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
    }
}

@Composable
fun GPSControlScreen(
    isSending: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
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
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (!isSending) {
            Button(onClick = onStartClick, modifier = Modifier.fillMaxWidth()) {
                Text("📡 GPS 전송 시작")
            }
        } else {
            Button(
                onClick = onStopClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("🛑 전송 중지")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("🛰 위치 데이터를 서버로 전송 중입니다...")
        }
    }
}
