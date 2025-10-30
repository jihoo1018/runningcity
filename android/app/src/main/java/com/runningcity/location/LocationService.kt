package com.runningcity.location

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.runningcity.R
import com.runningcity.network.service.LocationUploader
import kotlinx.coroutines.*

/**
 * LocationService
 * - ForegroundService로 실행되어 백그라운드에서도 위치 추적 가능
 * - 주기적으로 FusedLocationProviderClient를 통해 위치를 받아 Retrofit으로 서버 전송
 */
class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val uploader = LocationUploader()

    // 서비스 전체에서 사용하는 CoroutineScope 생성
    // SupervisorJob을 붙여서, 하나의 작업이 실패해도 전체가 죽지 않게 함
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val CHANNEL_ID = "running_location_channel"
        private const val NOTIFICATION_ID = 101
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel() // 알림 채널 생성 (Android 8+ 필수)
        startForeground(NOTIFICATION_ID, createNotification()) // 서비스 포그라운드 실행

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 위치 요청 설정
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // 3초마다 업데이트
        ).setMinUpdateDistanceMeters(5f)
            .build()

        // 위치 콜백 (위치 수신 시 실행)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    Log.d("LocationService", "📍 위치 수신: ${location.latitude}, ${location.longitude}")

                    // serviceScope를 사용해서 coroutine 실행
                    serviceScope.launch {
                        uploader.sendLocation(location)
                    }
                }
            }
        }
        startLocationUpdates()
    }

    /**
     * Android 8+ 알림 채널 생성
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "러닝 위치 추적",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "러닝 중 GPS를 실시간으로 추적합니다."
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * 상태바에 표시될 Foreground 알림 생성
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, Class.forName("com.runningcity.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("러닝시티")
            .setContentText("러닝 중 위치를 추적하고 있습니다 🏃‍♂️")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * FusedLocationProviderClient로 위치 추적 시작
     */
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.d("LocationService", "✅ Foreground 위치 추적 시작")
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // 서비스 종료 시 모든 코루틴 취소 (메모리 누수 방지)
        serviceScope.cancel()


        Log.d("LocationService", "🛑 Foreground 위치 추적 중지")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
