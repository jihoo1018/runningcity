package com.runningcity.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.local.entity.LocationRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WorkoutService : Service() {

    private val binder = WorkoutBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 위치 관련
    private var gpsListener: LocationListener? = null
    private var networkListener: LocationListener? = null
    private lateinit var locationManager: LocationManager

    // 현재 세션 정보
    var watchSessionId: String = ""
    var isTracking = false

    // ✅ 여기에 추가! (기존 변수들 바로 아래)
    // 거리 추적 변수
    private var totalDistance = 0f  // 총 거리 (미터)
    private var lastLocation: Location? = null  // 이전 위치

    // 위치 데이터 리스너
    var onLocationUpdate: ((Location) -> Unit)? = null
    // ✅ 여기에 추가!
    var onDistanceUpdate: ((Float) -> Unit)? = null  // 거리 업데이트 리스너

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "workout_channel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    inner class WorkoutBinder : Binder() {
        fun getService(): WorkoutService = this@WorkoutService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        println("🔧 WorkoutService onCreate")

        // 알림 채널 생성
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                println("▶️ WorkoutService 시작")
                startForegroundService()
                startLocationTracking()
            }
            ACTION_STOP -> {
                println("⏹️ WorkoutService 중지")
                stopLocationTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification("운동 추적 중...")
        startForeground(NOTIFICATION_ID, notification)
        println("🔔 Foreground Service 시작")
    }

    private fun startLocationTracking() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


        // ✅ GPS 시작할 때 거리 초기화 (여기에 추가!)
        resetDistance()

        // GPS 상태 확인
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        println("📡 [Service] GPS Provider: ${if(isGPSEnabled) "✅ ON" else "❌ OFF"}")
        println("📡 [Service] Network Provider: ${if(isNetworkEnabled) "✅ ON" else "❌ OFF"}")

        // 마지막 GPS 위치 확인
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val lastGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastGPS != null) {
                    val timeSince = System.currentTimeMillis() - lastGPS.time
                    println("📍 [Service] 마지막 GPS: ${lastGPS.accuracy}m (${timeSince/1000}초 전)")
                } else {
                    println("⚠️ [Service] 마지막 GPS 없음 (Cold Start - 위성 찾는데 2-3분 소요)")
                }
            }
        } catch (e: SecurityException) {
            println("❌ GPS 권한 에러")
        }

        // GPS 리스너 (정확한 위성 위치)
        gpsListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val accuracy = location.accuracy

                // GPS는 정확도 20m 이하만 사용
                if (accuracy <= 20f) {
                    println("📍 GPS (위성 ⭐): 위도=${String.format("%.6f", location.latitude)}, 경도=${String.format("%.6f", location.longitude)}, 정확도=${accuracy}m")
                    handleLocation(location, "GPS")
                } else {
                    println("⚠️ GPS 정확도 낮음: ${accuracy}m - 무시")
                }
            }

            override fun onProviderEnabled(provider: String) {
                println("✅ GPS 활성화됨: $provider")
            }

            override fun onProviderDisabled(provider: String) {
                println("❌ GPS 비활성화됨: $provider")
            }

            @Deprecated("Deprecated in API level 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {
                println("📡 GPS 상태 변경: $provider, status=$status")
            }
        }

        // Network 리스너 (A-GPS 백업용)
        networkListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val accuracy = location.accuracy

                // Network는 GPS 안 잡힐 때만 사용 (백업용)
                // 정확도 20m 초과만 사용 (GPS랑 겹치지 않게)
                if (accuracy > 20f) {
                    println("📍 Network (A-GPS 🌐): 위도=${String.format("%.6f", location.latitude)}, 경도=${String.format("%.6f", location.longitude)}, 정확도=${accuracy}m")
                    handleLocation(location, "Network")
                }
            }

            override fun onProviderEnabled(provider: String) {
                println("✅ Network 활성화됨: $provider")
            }

            override fun onProviderDisabled(provider: String) {
                println("❌ Network 비활성화됨: $provider")
            }

            @Deprecated("Deprecated in API level 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // GPS Provider 등록 (우선순위 1)
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,    // 1초마다
                    5f,       // 5미터 이동마다
                    gpsListener!!
                )
                println("🚀 GPS Provider 시작! (위성 신호 대기 중...)")
            } else {
                println("⚠️ GPS Provider 꺼져있음!")
            }

            // Network Provider 등록 (우선순위 2 - 백업용)
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000L,    // 5초마다 (GPS보다 느리게)
                    10f,      // 10미터 이동마다
                    networkListener!!
                )
                println("🚀 Network Provider 시작! (A-GPS 백업)")
            }

            isTracking = true
            println("🎯 하이브리드 GPS 추적 시작! (GPS 우선, Network 백업)")
        } else {
            println("❌ GPS 권한 없음 (Service)")
        }
    }

    // ✅ 새로운 코드로 교체
    private fun handleLocation(location: Location, source: String) {
        val latitude = location.latitude
        val longitude = location.longitude
        val accuracy = location.accuracy

        // ✅ 거리 계산 추가!
        if (lastLocation != null && accuracy <= 15f) {
            // 이전 위치와의 거리 계산
            val distance = lastLocation!!.distanceTo(location)

            // 너무 큰 점프는 무시 (GPS 오류)
            if (distance < 100f) {  // 100m 이상 점프는 오류
                totalDistance += distance

                println("📏 거리 증가: +${String.format("%.1f", distance)}m, 총: ${String.format("%.1f", totalDistance)}m")

                // UI 업데이트
                onDistanceUpdate?.invoke(totalDistance)
            } else {
                println("⚠️ 비정상적인 거리: ${String.format("%.1f", distance)}m - 무시")
            }
        }

        // 현재 위치를 이전 위치로 저장
        lastLocation = location

        // 알림 업데이트 (거리 추가)
        updateNotification("$source: ${String.format("%.1f", accuracy)}m | ${totalDistance.toInt()}m")

        // 리스너 호출 (WorkoutScreen 업데이트)
        onLocationUpdate?.invoke(location)

        // DB 저장
        if (isTracking && watchSessionId.isNotEmpty()) {
            saveLocationToDb(location)
        }
    }

    private fun stopLocationTracking() {
        if (gpsListener != null) {
            locationManager.removeUpdates(gpsListener!!)
            println("⏹️ GPS Provider 중지")
        }

        if (networkListener != null) {
            locationManager.removeUpdates(networkListener!!)
            println("⏹️ Network Provider 중지")
        }

        isTracking = false
        println("⏹️ 하이브리드 GPS 추적 중지")
    }

    private fun saveLocationToDb(location: Location) {
        serviceScope.launch {
            try {
                val database = WorkoutDatabase.getDatabase(applicationContext)
                val dao = database.workoutDao()

                val record = LocationRecordEntity(
                    watchSessionId = watchSessionId,
                    timestamp = System.currentTimeMillis(),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    altitude = location.altitude,
                    speed = location.speed
                )

                dao.insertLocation(record)
                println("💾 위치 DB 저장 (Service)")
            } catch (e: Exception) {
                println("❌ DB 저장 실패: ${e.message}")
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "운동 추적",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "운동 중 GPS 추적"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        println("📢 알림 채널 생성")
    }

    private fun createNotification(contentText: String): Notification {
        // 앱 실행 Intent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("러닝시티")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)  // GPS 아이콘
            .setOngoing(true)  // 스와이프로 제거 불가
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        println("🔧 WorkoutService onDestroy")
    }
    // 거리 초기화
    fun resetDistance() {
        totalDistance = 0f
        lastLocation = null
        println("🔄 거리 초기화")
    }

    // 현재 거리 가져오기
    fun getTotalDistance(): Float = totalDistance
}