package com.runningcity.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.runningcity.presentation.HeartRateMeasurementActivity
import com.runningcity.utils.MobileCommunicationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * HeartRateMeasurementService
 * 
 * 워치에서 심박수만 측정하는 서비스
 * (운동 시작 없이 단독으로 심박수 측정)
 */
class HeartRateMeasurementService : LifecycleService(), SensorEventListener {
    
    companion object {
        private const val TAG = "HeartRateMeasure"
        private const val MEASUREMENT_DURATION_MS = 10000L // 10초간 측정
        private const val MIN_MEASUREMENTS = 1 // 최소 측정 횟수 (1개 이상이면 사용)
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "heart_rate_measurement"
    }
    
    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
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
        
        // 권한 확인
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
        
        Log.d(TAG, "🔒 BODY_SENSORS 권한 상태: $hasPermission")
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("심박수 측정 준비 중..."))
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        
        if (heartRateSensor == null) {
            Log.e(TAG, "❌ 심박수 센서를 사용할 수 없습니다")
        } else {
            Log.d(TAG, "✅ 심박수 센서 확인됨: ${heartRateSensor?.name}")
        }
        
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
     * 심박수 측정 시작
     */
    private fun startMeasurement() {
        Log.d(TAG, "📞 startMeasurement() 호출됨")
        
        // 권한 재확인
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!hasPermission) {
            Log.e(TAG, "❌ BODY_SENSORS 권한이 없습니다")
            sendErrorToMobile("워치 앱을 열어 심박수 센서 권한을 허용해주세요")
            stopSelf()
            return
        }
        
        if (isMeasuring) {
            Log.w(TAG, "⚠️ 이미 측정 중입니다")
            return
        }
        
        if (heartRateSensor == null) {
            Log.e(TAG, "❌ 심박수 센서를 사용할 수 없습니다")
            sendErrorToMobile("이 기기는 심박수 센서를 지원하지 않습니다")
            stopSelf()
            return
        }
        
        // Activity는 이미 MobileMessageListenerService에서 시작되었으므로 여기서는 시작하지 않음
        // (중복 시작 방지)
        Log.d(TAG, "✅ 측정 화면은 이미 표시됨")
        
        isMeasuring = true
        heartRateValues.clear()
        
        // 센서 리스너 등록
        val registered = sensorManager.registerListener(
            this,
            heartRateSensor,
            SensorManager.SENSOR_DELAY_FASTEST  // 더 빠른 샘플링
        )
        
        if (!registered) {
            Log.e(TAG, "❌ 센서 리스너 등록 실패")
            sendErrorToMobile("센서 등록에 실패했습니다")
            isMeasuring = false
            stopSelf()
            return
        }
        
        Log.d(TAG, "✅ 센서 리스너 등록 성공")
        Log.d(TAG, "💓 심박수 측정 시작 (${MEASUREMENT_DURATION_MS}ms)")
        
        // Notification 업데이트
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification("심박수 측정 중... (${MEASUREMENT_DURATION_MS/1000}초)"))
        
        // 일정 시간 후 측정 종료
        measurementJob = CoroutineScope(Dispatchers.IO).launch {
            delay(MEASUREMENT_DURATION_MS)
            
            withContext(Dispatchers.Main) {
                Log.d(TAG, "⏰ 측정 시간 종료 - stopMeasurement 호출")
                stopMeasurement()
            }
        }
    }
    
    /**
     * 심박수 측정 종료
     */
    private fun stopMeasurement() {
        Log.d(TAG, "📞 stopMeasurement() 호출됨 - isMeasuring: $isMeasuring")
        
        if (!isMeasuring) {
            Log.w(TAG, "⚠️ 측정 중이 아님")
            stopSelf()
            return
        }
        
        isMeasuring = false
        measurementJob?.cancel()
        
        // 센서 리스너 해제
        sensorManager.unregisterListener(this)
        Log.d(TAG, "✅ 센서 리스너 해제 완료")
        
        // 측정값 처리
        Log.d(TAG, "📊 수집된 심박수 값: ${heartRateValues.size}개 - $heartRateValues")
        
        if (heartRateValues.isEmpty()) {
            Log.e(TAG, "❌ 측정된 심박수 값이 없습니다")
            
            // Activity에 에러 브로드캐스트 전송
            val errorIntent = Intent("com.runningcity.HEART_RATE_MEASUREMENT_ERROR")
            sendBroadcast(errorIntent)
            
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
        sendHeartRateToMobile(averageHeartRate)
    }
    
    /**
     * 센서 값 변경 콜백
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0].toInt()
            
            Log.d(TAG, "📡 센서 값 수신: $heartRate bpm (isMeasuring: $isMeasuring)")
            
            if (isMeasuring && heartRate > 0) {
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
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 정확도 변경 시 처리 (필요시)
    }
    
    /**
     * 모바일로 심박수 결과 전송
     */
    private fun sendHeartRateToMobile(heartRate: Int) {
        Log.d(TAG, "📤 모바일로 심박수 전송 시작: $heartRate bpm")
        
        CoroutineScope(Dispatchers.IO).launch {
            val success = MobileCommunicationHelper.sendHeartRateMeasurement(
                applicationContext,
                heartRate
            )
            
            if (success) {
                Log.d(TAG, "✅ 모바일로 심박수 전송 성공: $heartRate bpm")
            } else {
                Log.e(TAG, "❌ 모바일로 심박수 전송 실패")
            }
            
            // 전송 후 서비스 종료 (전송 완료 대기)
            delay(1000)
            withContext(Dispatchers.Main) {
                Log.d(TAG, "🛑 서비스 종료")
                stopSelf()
            }
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
        
        if (isMeasuring) {
            stopMeasurement()
        }
        
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 브로드캐스트 리시버 해제 실패: ${e.message}")
        }
        
        Log.d(TAG, "❌ HeartRateMeasurementService 종료")
    }
}

