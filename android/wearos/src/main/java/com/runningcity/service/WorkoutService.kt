package com.runningcity.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.runningcity.R
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.local.entity.LocationRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WorkoutService : Service() {

    private val binder = WorkoutBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 현재 세션 정보
    var sessionId: String = ""
    var isTracking = false

    // 위치 데이터 리스너
    var onLocationUpdate: ((Location) -> Unit)? = null

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val accuracy = location.accuracy

                    println("📍 GPS (Service): 위도=${String.format("%.6f", latitude)}, 경도=${String.format("%.6f", longitude)}, 정확도=${accuracy}m")

                    // 알림 업데이트
                    updateNotification("위도: ${String.format("%.6f", latitude)}")

                    // 리스너 호출
                    onLocationUpdate?.invoke(location)

                    // DB 저장
                    if (isTracking && sessionId.isNotEmpty()) {
                        saveLocationToDb(location)
                    }
                }
            }
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000  // 2초마다
        ).apply {
            setMinUpdateIntervalMillis(1000)  // 최소 1초
            setMaxUpdateDelayMillis(5000)
        }.build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            isTracking = true
            println("🚀 GPS 추적 시작 (Service)")
        } else {
            println("❌ GPS 권한 없음 (Service)")
        }
    }

    private fun stopLocationTracking() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isTracking = false
            println("⏹️ GPS 추적 중지 (Service)")
        }
    }

    private fun saveLocationToDb(location: Location) {
        serviceScope.launch {
            try {
                val database = WorkoutDatabase.getDatabase(applicationContext)
                val dao = database.workoutDao()

                val record = LocationRecordEntity(
                    sessionId = sessionId,
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
}