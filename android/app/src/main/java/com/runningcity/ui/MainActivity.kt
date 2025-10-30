package com.runningcity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.runningcity.location.LocationService

/**
 * MainActivity
 * - 앱 진입점(Activity)
 * - 위치 권한 요청 및 ForegroundService(LocationService) 실행 담당
 */
class MainActivity : ComponentActivity() {

    // 요청할 권한 목록 정의 (Android 10+에서는 ForegroundService 권한도 필요)
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,   // 정확한 위치 (GPS)
        Manifest.permission.ACCESS_COARSE_LOCATION, // 대략적 위치 (Wi-Fi, 셀룰러)
        Manifest.permission.FOREGROUND_SERVICE      // ForegroundService 실행 허용
    )

    // ActivityResult API를 이용해 권한 요청 결과 처리
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it } // 모든 권한이 허용되었는지 검사
        if (granted) startLocationService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 앱 실행 시 권한 요청 시작
        permissionLauncher.launch(permissions)
    }

    /**
     * ForegroundService(LocationService)를 실행하는 함수
     */
    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        startForegroundService(intent) // 앱이 백그라운드여도 실행 유지
    }
}
