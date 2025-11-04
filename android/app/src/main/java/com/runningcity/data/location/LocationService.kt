package com.runningcity.data.location

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.runningcity.R
import com.runningcity.data.location.LocationBufferManager
import com.runningcity.data.model.toLocation
import com.runningcity.data.network.LocationApiService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

@AndroidEntryPoint
class LocationService : Service() {

    @Inject lateinit var api: LocationApiService
    private lateinit var fusedClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 🔁 주기적 전송 간격
    private val UPLOAD_INTERVAL_MS = 300_000L // 300초 마다 = 5분
    private val UPLOAD_THRESHOLD = 100         // 100개 위치마다 전송


    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        startTracking()
        startAutoUploadLoop() // ✅ 주기적 전송 시작
    }

    private fun createNotification(): Notification =
        NotificationCompat.Builder(this, "running_location")
            .setContentTitle("러닝시티")
            .setContentText("러닝 세션 동안 위치 추적 중 🏃‍♂️")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

    private fun startTracking() {
        val req = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 3000L
        ).setMinUpdateDistanceMeters(5f).build() //3초마다, 최소 5m 이동 시 새 위치를 수집(→ onLocationResult()가 이 간격으로 계속 호출됨)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedClient.requestLocationUpdates(req, callback, Looper.getMainLooper())
    }

    /** 📍 위치 수집 콜백 */
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (loc in result.locations) {
                serviceScope.launch {
                    LocationBufferManager.addLocation(loc)

                    // ⚡ 즉시 업로드 조건: 100개 이상 쌓이면 자동 전송
                    if (LocationBufferManager.size() >= UPLOAD_THRESHOLD) {
                        uploadBufferedLocations(reason = "100개 초과")
                    }
                }
            }
        }
    }

    /** 🔁 300초(5분)마다 자동 업로드 루프 */
    private fun startAutoUploadLoop() {
        serviceScope.launch {
            while (isActive) {
                delay(UPLOAD_INTERVAL_MS)
                uploadBufferedLocations(reason = "300초 주기")
            }
        }
    }

    /** 🚀 버퍼 데이터 서버 전송 */
    private suspend fun uploadBufferedLocations(reason: String) {
        val locations = LocationBufferManager.flush()
        if (locations.isEmpty()) return

        try {
            // 서버 일시적 오류 (예: 네트워크 끊김) 시 1초 간격으로 3회까지 재시도
            retry(3) {
                api.sendLocationBatch(locations)
            }
            println("📡 [LocationService] ${locations.size}개 위치 전송 완료 ($reason)")
        } catch (e: Exception) { //3회 다 실패하면 오류 로그 출력 후 버퍼 복원 (데이터 손실 방지)
            println("⚠️ [LocationService] 위치 전송 실패 ($reason): ${e.message}")
            // 실패 시 복원 (선택)
            locations.forEach { LocationBufferManager.addLocation(it.toLocation()) }
        }
    }

    /** 🔁 코루틴 재시도 유틸 함수 */
    suspend fun <T> retry(
        times: Int,
        block: suspend () -> T
    ): T {
        var lastError: Throwable? = null
        repeat(times) { attempt ->
            try {
                return block()
            } catch (e: Throwable) {
                lastError = e
                println("⚠️ [Retry] ${attempt + 1}번째 시도 실패: ${e.message}")
                delay(1000L) // 재시도 간격 (1초)
            }
        }
        throw lastError ?: IllegalStateException("Retry 실패")
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(callback)

        // 💾 세션 종료 시 남은 위치 마지막 전송
        serviceScope.launch {
            uploadBufferedLocations(reason = "세션 종료")
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
