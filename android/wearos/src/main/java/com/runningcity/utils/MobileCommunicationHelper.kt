package com.runningcity.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import kotlinx.coroutines.tasks.await

/**
 * MobileCommunicationHelper
 * 
 * 워치에서 모바일로 메시지를 전송하는 헬퍼 클래스
 */
object MobileCommunicationHelper {
    
    private const val TAG = "MobileComm"
    
    /**
     * 모바일에 데이터 준비 완료 알림
     * (워치에서 중지 버튼을 누르면 호출)
     */
    suspend fun notifyDataReady(context: Context): Boolean {
        return try {
            val nodeClient = Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.await()
            
            if (nodes.isEmpty()) {
                Log.d(TAG, "연결된 모바일이 없습니다")
                return false
            }
            
            val messageClient = Wearable.getMessageClient(context)
            
            var success = false
            nodes.forEach { node ->
                try {
                    messageClient.sendMessage(node.id, "/watch_data_ready", byteArrayOf()).await()
                    Log.d(TAG, "✅ 모바일에 데이터 준비 완료 메시지 전송 (node: ${node.displayName})")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 모바일 메시지 전송 실패: ${e.message}")
                }
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ 모바일 통신 오류: ${e.message}", e)
            false
        }
    }
    
    /**
     * 모바일에 운동 종료 알림
     * (모바일에서 시작한 운동을 워치에서 종료할 때 호출)
     */
    suspend fun notifyWorkoutStopped(context: Context, sessionId: Long): Boolean {
        return try {
            val nodeClient = Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.await()
            
            if (nodes.isEmpty()) {
                Log.d(TAG, "연결된 모바일이 없습니다")
                return false
            }
            
            val messageClient = Wearable.getMessageClient(context)
            val message = sessionId.toString().toByteArray()
            
            var success = false
            nodes.forEach { node ->
                try {
                    messageClient.sendMessage(node.id, "/workout_stopped", message).await()
                    Log.d(TAG, "✅ 모바일에 운동 종료 알림 전송 (sessionId: $sessionId, node: ${node.displayName})")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 모바일 메시지 전송 실패: ${e.message}")
                }
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ 모바일 통신 오류: ${e.message}", e)
            false
        }
    }
}

