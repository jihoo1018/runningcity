package com.runningcity.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.sync.SyncResult
import com.runningcity.data.sync.WatchDataSyncRepository
import kotlinx.coroutines.*

// 워치 -> 모바일 데이터 전달하기 위해 추가한 부분
/**
 * DataSyncService
 * 
 * 워치에서 백그라운드로 데이터를 모바일에 동기화하는 서비스
 * 
 * 동작 방식:
 * 
 * 1️⃣ 시작 버튼 클릭 시
 *    - 워치 세션 ID 생성 및 저장
 *    - 로컬 DB에 데이터 계속 쌓음
 *    - 안드로이드에는 데이터 전송하지 않음
 * 
 * 2️⃣ 중지 버튼 클릭 시
 *    - 세션 동안 쌓인 모든 데이터를 한 번에 전송
 *    - 전송 데이터: heartRates, locations, cadences, calories
 */
class DataSyncService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private lateinit var syncRepository: WatchDataSyncRepository
    
    // 현재 운동 세션 ID (운동 시작 시 설정됨)
    private var currentWatchSessionId: String? = null
    
    companion object {
        private const val TAG = "DataSyncService"
        
        // Notification
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "data_sync_channel"
        
        // Action
        const val ACTION_START_SESSION = "ACTION_START_SESSION"
        const val ACTION_STOP_SESSION = "ACTION_STOP_SESSION"
        const val EXTRA_WATCH_SESSION_ID = "WATCH_SESSION_ID"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Repository 초기화
        val database = WorkoutDatabase.getDatabase(applicationContext)
        syncRepository = WatchDataSyncRepository(applicationContext, database)
        
        // Foreground Service 시작
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        Log.d(TAG, "✅ DataSyncService 생성됨 (Foreground)")
    }
    
    /**
     * 알림 채널 생성 (Android O 이상)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "데이터 동기화",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "워치 데이터를 모바일로 전송 중"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 알림 생성
     */
    private fun createNotification(message: String = "대기 중..."): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("데이터 동기화")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    /**
     * 알림 업데이트
     */
    private fun updateNotification(message: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(message))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SESSION -> {
                // 운동 시작 - 세션 ID 저장만 함
                val sessionId = intent.getStringExtra(EXTRA_WATCH_SESSION_ID)
                currentWatchSessionId = sessionId
                Log.d(TAG, "🚀 운동 시작 - 세션 ID: $sessionId")
                updateNotification("운동 중... (세션: $sessionId)")
            }
            
            ACTION_STOP_SESSION -> {
                // 운동 중지 - 모든 데이터 한 번에 전송
                currentWatchSessionId?.let { sessionId ->
                    Log.d(TAG, "⏹️ 운동 중지 - 데이터 전송 시작 (세션: $sessionId)")
                    updateNotification("데이터 전송 중...")
                    serviceScope.launch {
                        syncAllSessionData(sessionId)
                    }
                }
            }
        }
        
        return START_STICKY // 시스템에 의해 종료되어도 재시작
    }

    /**
     * 세션 데이터 한 번에 전송
     * 중지 버튼 클릭 시 호출됨
     */
    private suspend fun syncAllSessionData(sessionId: String) {
        try {
            // 1. 연결 상태 확인
            val isConnected = syncRepository.isMobileReachable()
            if (!isConnected) {
                Log.e(TAG, "❌ 모바일 연결 안 됨 - 데이터 전송 실패")
                updateNotification("전송 실패 (연결 안 됨)")
                return
            }
            
            // 2. 해당 세션의 모든 데이터 한 번에 전송
            Log.d(TAG, "📤 세션 데이터 전송 시작 (세션: $sessionId)")
            
            val result = syncRepository.syncWorkoutData(
                watchSessionId = sessionId,
                batchSize = Int.MAX_VALUE // 모든 데이터 한 번에 전송
            )
            
            // 3. 결과 로그 출력
            when (result) {
                is SyncResult.Success -> {
                    Log.d(TAG, """
                        ✅ 데이터 전송 성공
                        {
                        	"clientSecretKey": "${result.clientSecretKey}",
                        	"cadences": ${result.cadenceCount}개,
                        	"heartRates": ${result.heartRateCount}개,
                        	"gpsPoints": ${result.gpsCount}개
                        }
                    """.trimIndent())
                    updateNotification("전송 완료")
                }
                
                is SyncResult.Queued -> {
                    Log.d(TAG, "📮 Data Layer 큐에 추가됨")
                    updateNotification("전송 대기 중")
                }
                
                is SyncResult.NoData -> {
                    Log.d(TAG, "📭 전송할 데이터 없음")
                    updateNotification("전송할 데이터 없음")
                }
                
                is SyncResult.Error -> {
                    Log.e(TAG, "❌ 전송 실패: ${result.message}")
                    updateNotification("전송 실패")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 데이터 전송 중 오류: ${e.message}", e)
            updateNotification("전송 오류")
        }
    }

    /**
     * 서비스 중지
     */
    fun stopSync() {
        Log.d(TAG, "⏹️ DataSyncService 중지 요청")
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "❌ DataSyncService 종료됨")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

