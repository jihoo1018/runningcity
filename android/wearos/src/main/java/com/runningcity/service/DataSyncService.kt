package com.runningcity.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.sync.SyncResult
import com.runningcity.data.sync.WatchDataSyncRepository
import kotlinx.coroutines.*

// 워치 -> 모바일 데이터 전달하기 위해 추가한 부분
/**
 * DataSyncService
 * 
 * 워치에서 백그라운드로 데이터를 모바일에 동기화하는 서비스
 * WearableListenerService를 상속하여 모바일로부터 메시지를 직접 수신
 * 
 * 동작 방식:
 * 
 * 1️⃣ 시작 버튼 클릭 시
 *    - 워치 세션 ID 생성 및 저장
 *    - 로컬 DB에 데이터 계속 쌓음
 *    - 안드로이드에는 데이터 전송하지 않음
 * 
 * 2️⃣ 중지 버튼 클릭 시
 *    - 모바일에 데이터 준비 알림 전송
 *    - 모바일 준비 완료 메시지 대기
 * 
 * 3️⃣ 모바일 준비 완료 시
 *    - 세션 동안 쌓인 모든 데이터를 한 번에 전송
 *    - 전송 데이터: heartRates, locations, cadences, calories
 */
class DataSyncService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private lateinit var syncRepository: WatchDataSyncRepository
    private lateinit var database: WorkoutDatabase
    
    companion object {
        private const val TAG = "DataSyncService"
        
        // Notification
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "data_sync_channel"
        
        // Message Path
        private const val PATH_MOBILE_READY = "/mobile_ready"
        private const val PATH_SYNC_REQUEST = "/sync_request"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Repository 초기화
        database = WorkoutDatabase.getDatabase(applicationContext)
        syncRepository = WatchDataSyncRepository(applicationContext, database)
        
        // Foreground Service 시작
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        Log.d(TAG, "✅ DataSyncService 생성됨 (Foreground) - 모바일 요청 대기 중")
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

    /**
     * 모바일로부터 메시지 수신
     */
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "📨 메시지 수신: ${messageEvent.path}")
        
        when (messageEvent.path) {
            PATH_MOBILE_READY, PATH_SYNC_REQUEST -> {
                Log.d(TAG, "✅ 모바일에서 동기화 요청 수신")
                // 미동기화 데이터 확인 및 전송
                serviceScope.launch {
                    syncUnsyncedData()
                }
            }
        }
    }
    
    /**
     * 미동기화 데이터 확인 및 전송
     */
    private suspend fun syncUnsyncedData() {
        try {
            Log.d(TAG, "🔍 미동기화 세션 확인 중...")
            
            // 1. 미동기화 세션 조회
            val unsyncedSessions = database.workoutDao().getUnsyncedSessions()
            
            if (unsyncedSessions.isEmpty()) {
                Log.d(TAG, "📭 미동기화 세션 없음")
                updateNotification("동기화 완료")
                return
            }
            
            Log.d(TAG, "📦 미동기화 세션 ${unsyncedSessions.size}개 발견")
            
            // 2. 각 세션 데이터 전송
            unsyncedSessions.forEach { session ->
                Log.d(TAG, "📤 세션 전송 시작: ${session.clientSecretKey}")
                updateNotification("동기화 중... (${session.clientSecretKey})")
                syncAllSessionData(session.clientSecretKey)
            }
            
            Log.d(TAG, "✅ 모든 세션 동기화 완료")
            updateNotification("동기화 완료")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 동기화 오류: ${e.message}", e)
            updateNotification("동기화 오류")
        }
    }

    /**
     * 세션 데이터 한 번에 전송
     */
    private suspend fun syncAllSessionData(sessionId: String) {
        try {
            // 1. 해당 세션의 모든 데이터 한 번에 전송
            Log.d(TAG, "📤 세션 데이터 전송 시작 (세션: $sessionId)")
            
            val result = syncRepository.syncWorkoutData(
                watchSessionId = sessionId,
                batchSize = Int.MAX_VALUE // 모든 데이터 한 번에 전송
            )
            
            // 2. 결과 로그 출력
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
                    
                    // 세션을 동기화 완료로 마킹
                    database.workoutDao().markSessionAsSynced(sessionId)
                    Log.d(TAG, "✅ 세션 동기화 완료 마킹: $sessionId")
                }
                
                is SyncResult.Queued -> {
                    Log.d(TAG, "📮 Data Layer 큐에 추가됨")
                }
                
                is SyncResult.NoData -> {
                    Log.d(TAG, "📭 전송할 데이터 없음")
                }
                
                is SyncResult.Error -> {
                    Log.e(TAG, "❌ 전송 실패: ${result.message}")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 데이터 전송 중 오류: ${e.message}", e)
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
}

