package com.runningcity.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.*
import androidx.health.services.client.endExercise
import androidx.lifecycle.LifecycleService
import com.runningcity.utils.MobileCommunicationHelper
import kotlinx.coroutines.*

/**
 * HeartRateMeasurementService
 * 
 * 워치에서 심박수만 측정하는 서비스
 * Health Services를 사용하여 심박수 측정 (WorkoutService와 동일한 방식)
 */
class HeartRateMeasurementService : LifecycleService() {
    
    companion object {
        private const val TAG = "HeartRateMeasure"
        private const val MEASUREMENT_DURATION_MS = 15000L // 15초간 측정
        private const val MIN_MEASUREMENTS = 1 // 최소 측정 횟수 (1개 이상이면 사용)
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "heart_rate_measurement"
    }
    
    // Health Services 클라이언트
    private lateinit var exerciseClient: ExerciseClient
    private var exerciseCallback: ExerciseUpdateCallback? = null
    private var isExerciseActive = false
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var measurementJob: Job? = null
    
    private val heartRateValues = mutableListOf<Int>()
    private var isMeasuring = false
    
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.runningcity.MEASURE_HEART_RATE" -> {
                    Log.d(TAG, "💓 심박수 측정 요청 수신")
                    // 이미 측정 중이면 중지 후 재시작
                    if (isMeasuring) {
                        Log.d(TAG, "⚠️ 이미 측정 중이므로 중지 후 재시작")
                        stopMeasurement()
                        // 잠시 후 재시작
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            startMeasurement()
                        }
                    } else {
                        startMeasurement()
                    }
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "✅ HeartRateMeasurementService 생성")
        
        // Health Services 초기화
        val healthServicesClient = HealthServices.getClient(this)
        exerciseClient = healthServicesClient.exerciseClient
        
        Log.d(TAG, "✅ Health Services 초기화 완료")
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("심박수 측정 준비 중..."))
        
        // 브로드캐스트 리시버 등록
        val filter = IntentFilter().apply {
            addAction("com.runningcity.MEASURE_HEART_RATE")
        }
        registerReceiver(broadcastReceiver, filter)
    }
    
    /**
     * Notification 채널 생성
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "심박수 측정",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "심박수 측정 중"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Notification 생성
     */
    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("심박수 측정")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }
    
    /**
     * 심박수 측정 시작 (Health Services 사용)
     */
    private fun startMeasurement() {
        Log.d(TAG, "📞 startMeasurement() 호출됨 - Health Services 사용")
        
        if (isMeasuring) {
            Log.w(TAG, "⚠️ 이미 측정 중입니다")
            return
        }
        
        // Activity는 이미 MobileMessageListenerService에서 시작되었으므로 여기서는 시작하지 않음
        Log.d(TAG, "✅ 측정 화면은 이미 표시됨")
        
        isMeasuring = true
        heartRateValues.clear()
        
        // Health Services를 사용한 심박수 측정 시작
        startHealthServicesMeasurement()
    }
    
    /**
     * Health Services를 사용한 심박수 측정 시작
     */
    private fun startHealthServicesMeasurement() {
        serviceScope.launch {
            try {
                Log.d(TAG, "🏃 Health Services 운동 시작 (심박수 측정용)")
                
                // ExerciseConfig 생성 - 심박수만 수집
                val config = ExerciseConfig(
                    exerciseType = ExerciseType.RUNNING,
                    dataTypes = setOf(DataType.HEART_RATE_BPM),  // 심박수만 수집
                    isAutoPauseAndResumeEnabled = false,
                    isGpsEnabled = false
                )
                
                // 콜백 생성
                val callback = object : ExerciseUpdateCallback {
                    override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                        processExerciseUpdate(update)
                    }
                    
                    override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
                        // 사용 안 함
                    }
                    
                    override fun onRegistered() {
                        Log.d(TAG, "✅ Exercise Callback 등록 완료")
                    }
                    
                    override fun onRegistrationFailed(throwable: Throwable) {
                        Log.e(TAG, "❌ Exercise Callback 등록 실패: ${throwable.message}")
                        serviceScope.launch {
                            sendErrorToMobile("심박수 측정을 시작할 수 없습니다: ${throwable.message}")
                            withContext(Dispatchers.Main) {
                                stopMeasurement()
                            }
                        }
                    }
                    
                    override fun onAvailabilityChanged(
                        dataType: DataType<*, *>,
                        availability: Availability
                    ) {
                        Log.d(TAG, "📍 센서 상태 변경: $dataType = $availability")
                    }
                }
                
                exerciseCallback = callback
                exerciseClient.setUpdateCallback(callback)
                exerciseClient.startExerciseAsync(config).get()
                isExerciseActive = true
                
                Log.d(TAG, "✅ Health Services 운동 시작 완료")
                Log.d(TAG, "💓 심박수 측정 시작 (${MEASUREMENT_DURATION_MS}ms)")
                
                // Notification 업데이트
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification("심박수 측정 중... (${MEASUREMENT_DURATION_MS / 1000}초)")
                )
                
                // 일정 시간 후 측정 종료
                measurementJob = launch {
                    delay(MEASUREMENT_DURATION_MS)
                    
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "⏰ 측정 시간 종료 - stopMeasurement 호출")
                        stopMeasurement()
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Health Services 시작 실패: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    sendErrorToMobile("심박수 측정을 시작할 수 없습니다: ${e.message}")
                    stopMeasurement()
                }
            }
        }
    }
    
    /**
     * Health Services에서 받은 데이터 처리
     */
    private fun processExerciseUpdate(update: ExerciseUpdate) {
        if (!isMeasuring) return
        
        try {
            val latestMetrics = update.latestMetrics
            
            // 심박수 처리
            val heartRateData = latestMetrics.getData(DataType.HEART_RATE_BPM)
            val heartRateList = heartRateData.toList()
            
            if (heartRateList.isNotEmpty()) {
                val heartRate = heartRateList.last().value.toInt()
                
                if (heartRate > 0) {
                    heartRateValues.add(heartRate)
                    Log.d(TAG, "💓 심박수 측정: $heartRate bpm (총 ${heartRateValues.size}개)")
                    
                    // Activity에 실시간 업데이트 브로드캐스트 전송
                    val updateIntent = Intent("com.runningcity.HEART_RATE_MEASUREMENT_UPDATE").apply {
                        putExtra("heartRate", heartRate)
                    }
                    sendBroadcast(updateIntent)
                    
                    // Notification 업데이트
                    val notificationManager = getSystemService(NotificationManager::class.java)
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        createNotification("측정 중: $heartRate bpm (${heartRateValues.size}개)")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exercise Update 처리 실패: ${e.message}")
        }
    }
    
    /**
     * 심박수 측정 종료
     */
    private fun stopMeasurement() {
        Log.d(TAG, "📞 stopMeasurement() 호출됨 - isMeasuring: $isMeasuring")
        
        if (!isMeasuring && !isExerciseActive) {
            Log.w(TAG, "⚠️ 측정 중이 아님")
            stopSelf()
            return
        }
        
        isMeasuring = false
        measurementJob?.cancel()
        
        // Health Services 정리
        stopHealthServicesTracking()
        
        // 측정값 처리
        Log.d(TAG, "📊 수집된 심박수 값: ${heartRateValues.size}개 - $heartRateValues")
        
        if (heartRateValues.isEmpty()) {
            Log.e(TAG, "❌ 측정된 심박수 값이 없습니다")
            
            // Activity에 에러 브로드캐스트 전송
            val errorIntent = Intent("com.runningcity.HEART_RATE_MEASUREMENT_ERROR")
            sendBroadcast(errorIntent)
            
            // 모바일로 에러 전송
            sendErrorToMobile("심박수 측정에 실패했습니다. 워치를 손목에 착용했는지 확인해주세요.")
            
            // 서비스 종료를 지연시켜 메시지 전송 완료 보장
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                stopSelf()
            }
            return
        }
        
        // 유효한 값만 필터링 (0보다 큰 값)
        val validValues = heartRateValues.filter { it > 0 }
        
        Log.d(TAG, "✅ 유효한 심박수 값: ${validValues.size}개 - $validValues")
        
        if (validValues.size < MIN_MEASUREMENTS) {
            Log.e(TAG, "❌ 충분한 측정값이 없습니다 (${validValues.size}개, 최소 ${MIN_MEASUREMENTS}개 필요)")
            
            // Activity에 에러 브로드캐스트 전송
            val errorIntent = Intent("com.runningcity.HEART_RATE_MEASUREMENT_ERROR")
            sendBroadcast(errorIntent)
            
            // 모바일로 에러 전송
            sendErrorToMobile("충분한 측정값을 얻지 못했습니다. 다시 시도해주세요.")
            
            // 서비스 종료를 지연시켜 메시지 전송 완료 보장
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                stopSelf()
            }
            return
        }
        
        // 평균값 계산
        val averageHeartRate = validValues.average().toInt()
        
        Log.d(TAG, "✅ 심박수 측정 완료: $averageHeartRate bpm (${validValues.size}개 값 사용)")
        
        // Activity에 완료 브로드캐스트 전송
        val completeIntent = Intent("com.runningcity.HEART_RATE_MEASUREMENT_COMPLETE").apply {
            putExtra("heartRate", averageHeartRate)
        }
        sendBroadcast(completeIntent)
        
        // 모바일로 결과 전송
        serviceScope.launch {
            sendHeartRateToMobile(averageHeartRate)
            // 전송 후 서비스 종료
            delay(500)
            withContext(Dispatchers.Main) {
                Log.d(TAG, "🛑 서비스 종료")
                stopSelf()
            }
        }
    }
    
    /**
     * Health Services 정리
     */
    private fun stopHealthServicesTracking() {
        if (!isExerciseActive) {
            Log.d(TAG, "⚠️ Health Services가 활성화되지 않음")
            return
        }
        
        serviceScope.launch {
            try {
                exerciseClient.endExercise()
                
                exerciseCallback?.let { callback ->
                    exerciseClient.clearUpdateCallback(callback)
                }
                
                isExerciseActive = false
                Log.d(TAG, "✅ Health Services 종료 완료")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Health Services 종료 실패: ${e.message}")
            }
        }
    }
    
    /**
     * 모바일로 심박수 결과 전송
     */
    private suspend fun sendHeartRateToMobile(heartRate: Int) {
        Log.d(TAG, "📤 모바일로 심박수 전송 시작: $heartRate bpm")
        
        try {
            val success = MobileCommunicationHelper.sendHeartRateMeasurement(
                applicationContext,
                heartRate
            )
            
            if (success) {
                Log.d(TAG, "✅ 모바일로 심박수 전송 성공: $heartRate bpm")
            } else {
                Log.e(TAG, "❌ 모바일로 심박수 전송 실패")
            }
            
            // 전송 완료 대기
            delay(500)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 모바일로 심박수 전송 중 에러: ${e.message}")
        }
    }
    
    /**
     * 모바일로 에러 메시지 전송
     */
    private fun sendErrorToMobile(message: String) {
        Log.e(TAG, "📤 모바일로 에러 전송: $message")
        
        CoroutineScope(Dispatchers.IO).launch {
            val success = MobileCommunicationHelper.sendHeartRateError(
                applicationContext,
                message
            )
            
            if (success) {
                Log.d(TAG, "✅ 모바일로 에러 전송 성공")
            } else {
                Log.e(TAG, "❌ 모바일로 에러 전송 실패")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        if (isMeasuring || isExerciseActive) {
            stopMeasurement()
        }
        
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 브로드캐스트 리시버 해제 실패: ${e.message}")
        }
        
        serviceScope.cancel()
        
        Log.d(TAG, "❌ HeartRateMeasurementService 종료")
    }
}

