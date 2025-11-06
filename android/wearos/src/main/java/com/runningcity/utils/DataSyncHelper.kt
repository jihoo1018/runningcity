package com.runningcity.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.runningcity.service.DataSyncService

// 워치 -> 모바일 데이터 전달하기 위해 추가한 부분
/**
 * DataSyncHelper
 * 
 * 데이터 동기화 서비스를 쉽게 시작/중지하는 헬퍼 클래스
 * 
 * 사용 예시:
 * ```kotlin
 * // 운동 시작 시 (세션 ID만 저장)
 * DataSyncHelper.startSession(context, watchSessionId)
 * 
 * // 운동 중지 시 (데이터 전송)
 * DataSyncHelper.stopSession(context)
 * ```
 */
object DataSyncHelper {
    
    private const val TAG = "DataSyncHelper"
    
    /**
     * 운동 세션 시작
     * 
     * 시작 버튼 클릭 시 호출하면 됩니다.
     * 세션 ID를 저장만 하고, 데이터는 전송하지 않습니다.
     * 
     * @param context Context
     * @param watchSessionId 현재 운동 세션 ID
     */
    fun startSession(context: Context, watchSessionId: String) {
        try {
            val intent = Intent(context, DataSyncService::class.java).apply {
                action = DataSyncService.ACTION_START_SESSION
                putExtra(DataSyncService.EXTRA_WATCH_SESSION_ID, watchSessionId)
            }
            ContextCompat.startForegroundService(context, intent)
            Log.d(TAG, "🚀 운동 세션 시작 (세션: $watchSessionId)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 세션 시작 실패: ${e.message}", e)
        }
    }
    
    /**
     * 운동 세션 중지 및 데이터 전송
     * 
     * 중지 버튼 클릭 시 호출하면 됩니다.
     * 세션 동안 쌓인 모든 데이터를 한 번에 전송합니다.
     */
    fun stopSession(context: Context) {
        try {
            val intent = Intent(context, DataSyncService::class.java).apply {
                action = DataSyncService.ACTION_STOP_SESSION
            }
            ContextCompat.startForegroundService(context, intent)
            Log.d(TAG, "⏹️ 운동 세션 중지 - 데이터 전송 시작")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 세션 중지 실패: ${e.message}", e)
        }
    }
}

