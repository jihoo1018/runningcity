package com.runningcity.service

import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.runningcity.presentation.HeartRateMeasurementActivity
import com.runningcity.presentation.RunningActivity
import com.runningcity.service.HeartRateMeasurementService

// 모바일 -> 워치로 통신하기 위한 코드
/**
 * MobileMessageListenerService
 * 
 * 모바일로부터 메시지를 수신하는 서비스
 * 
 * 동작:
 * 1. 모바일에서 "운동 시작" 메시지 수신 → WorkoutService 시작
 * 2. 모바일에서 "운동 중지" 메시지 수신 → WorkoutService 중지
 */
class MobileMessageListenerService : WearableListenerService() {
    
    companion object {
        private const val TAG = "MobileMessageListener"
        
        // 메시지 경로
        private const val PATH_START_WORKOUT = "/start_workout"
        private const val PATH_STOP_WORKOUT = "/stop_workout"
        private const val PATH_PAUSE_WORKOUT = "/pause_workout"
        private const val PATH_RESUME_WORKOUT = "/resume_workout"
        private const val PATH_MOBILE_READY = "/mobile_ready"
        private const val PATH_MEASURE_HEART_RATE = "/measure_heart_rate"
        private const val PATH_PAIR_WATCH = "/pair_watch"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "✅ MobileMessageListenerService 시작됨")
    }
    
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "📨 메시지 수신: ${messageEvent.path}")
        
        when (messageEvent.path) {
            PATH_START_WORKOUT -> {
                // 세션 ID 파싱
                val sessionId = String(messageEvent.data, Charsets.UTF_8).toLongOrNull()
                
                if (sessionId != null) {
                    Log.d(TAG, "🚀 운동 시작 요청 (세션 ID: $sessionId)")
                    startWorkoutFromMobile(sessionId)
                } else {
                    Log.e(TAG, "❌ 세션 ID 파싱 실패")
                }
            }
            
            PATH_STOP_WORKOUT -> {
                Log.d(TAG, "⏹️ 운동 중지 요청")
                stopWorkoutFromMobile()
            }
            
            PATH_PAUSE_WORKOUT -> {
                Log.d(TAG, "⏸️ 운동 일시정지 요청")
                pauseWorkoutFromMobile()
            }
            
            PATH_RESUME_WORKOUT -> {
                Log.d(TAG, "▶️ 운동 재개 요청")
                resumeWorkoutFromMobile()
            }
            
            PATH_MOBILE_READY -> {
                Log.d(TAG, "✅ 모바일 준비 완료")
                notifyMobileReady()
            }
            
            PATH_MEASURE_HEART_RATE -> {
                Log.d(TAG, "💓 심박수 측정 요청 수신")
                startHeartRateMeasurement()
            }
            
            PATH_PAIR_WATCH -> {
                Log.d(TAG, "⌚ 워치 연동 요청 수신")
                startWatchPairing()
            }
            
            else -> {
                Log.w(TAG, "⚠️ 알 수 없는 메시지 경로: ${messageEvent.path}")
            }
        }
    }
    
    /**
     * 모바일에서 시작된 운동 시작
     */
    private fun startWorkoutFromMobile(sessionId: Long) {
        // 1. 워치 앱 자동 시작
        val activityIntent = Intent(this, RunningActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("sessionId", sessionId)
            putExtra("autoStart", true)  // 자동 시작 플래그
        }
        startActivity(activityIntent)
        Log.d(TAG, "🚀 워치 앱 자동 시작 (세션: $sessionId)")
        
        // 2. WorkoutScreen의 시작 로직을 트리거하기 위해 브로드캐스트 전송
        val broadcastIntent = Intent("com.runningcity.START_WORKOUT_FROM_MOBILE")
        broadcastIntent.putExtra("sessionId", sessionId)
        sendBroadcast(broadcastIntent)
        
        Log.d(TAG, "✅ 워치 운동 시작 브로드캐스트 전송 (세션: $sessionId)")
    }
    
    /**
     * 모바일에서 시작된 운동 중지
     */
    private fun stopWorkoutFromMobile() {
        // WorkoutScreen의 중지 로직을 트리거하기 위해 브로드캐스트 전송
        val intent = Intent("com.runningcity.STOP_WORKOUT_FROM_MOBILE")
        sendBroadcast(intent)
        
        Log.d(TAG, "✅ 워치 운동 중지 브로드캐스트 전송")
    }
    
    /**
     * 모바일에서 시작된 운동 일시정지
     */
    private fun pauseWorkoutFromMobile() {
        // WorkoutScreen의 일시정지 로직을 트리거하기 위해 브로드캐스트 전송
        val intent = Intent("com.runningcity.PAUSE_WORKOUT_FROM_MOBILE")
        sendBroadcast(intent)
        
        Log.d(TAG, "✅ 워치 운동 일시정지 브로드캐스트 전송")
    }
    
    /**
     * 모바일에서 시작된 운동 재개
     */
    private fun resumeWorkoutFromMobile() {
        // WorkoutScreen의 재개 로직을 트리거하기 위해 브로드캐스트 전송
        val intent = Intent("com.runningcity.RESUME_WORKOUT_FROM_MOBILE")
        sendBroadcast(intent)
        
        Log.d(TAG, "✅ 워치 운동 재개 브로드캐스트 전송")
    }
    
    /**
     * 모바일 준비 완료 알림
     */
    private fun notifyMobileReady() {
        // WorkoutScreen에 모바일 준비 완료 브로드캐스트 전송
        val intent = Intent("com.runningcity.MOBILE_READY")
        sendBroadcast(intent)
        
        Log.d(TAG, "✅ 모바일 준비 완료 브로드캐스트 전송")
    }
    
    /**
     * 심박수 측정 시작
     */
    private fun startHeartRateMeasurement() {
        Log.d(TAG, "💓 심박수 측정 요청 수신 - 즉시 화면 표시")
        
        // 1. 먼저 화면을 바로 열기 (사용자가 즉시 확인 가능)
        val activityIntent = Intent(this, HeartRateMeasurementActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(activityIntent)
        Log.d(TAG, "✅ 측정 화면 Activity 즉시 시작")
        
        // 2. HeartRateMeasurementService 시작
        val serviceIntent = Intent(this, HeartRateMeasurementService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        
        // 3. 측정 요청 브로드캐스트 전송
        val intent = Intent("com.runningcity.MEASURE_HEART_RATE")
        sendBroadcast(intent)
        
        Log.d(TAG, "✅ 심박수 측정 서비스 시작 및 브로드캐스트 전송")
    }
    
    /**
     * 워치 연동 시작
     */
    private fun startWatchPairing() {
        Log.d(TAG, "⌚ 워치 연동 요청 수신 - 화면 표시")
        
        // 워치 화면 깨우기 (닫혀있어도 켜지도록)
        wakeUpScreen()
        
        // WatchPairingActivity 시작 (화면 켜기 플래그 포함)
        try {
            val activityIntent = Intent(this, com.runningcity.presentation.WatchPairingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(activityIntent)
            Log.d(TAG, "✅ 워치 연동 화면 Activity 시작")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 워치 연동 화면 Activity 시작 실패: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 워치 화면 깨우기
     */
    private fun wakeUpScreen() {
        try {
            val powerManager = getSystemService(PowerManager::class.java)
            if (powerManager != null && !powerManager.isInteractive) {
                // 화면이 꺼져있으면 깨우기
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "WatchPairing::WakeLock"
                )
                wakeLock.acquire(100) // 짧게 acquire
                wakeLock.release() // 즉시 release (화면만 켜기)
                Log.d(TAG, "✅ 워치 화면 깨우기 완료")
            } else {
                Log.d(TAG, "✅ 워치 화면이 이미 켜져있음")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 워치 화면 깨우기 실패: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "❌ MobileMessageListenerService 종료됨")
    }
}

