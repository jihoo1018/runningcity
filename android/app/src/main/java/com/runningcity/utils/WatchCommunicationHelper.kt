package com.runningcity.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import kotlinx.coroutines.tasks.await

/**
 * WatchCommunicationHelper
 * 
 * 모바일에서 워치로 메시지를 전송하는 헬퍼 클래스
 */
object WatchCommunicationHelper {
    
    private const val TAG = "WatchComm"
    
    // 메시지 경로
    private const val PATH_START_WORKOUT = "/start_workout"
    private const val PATH_STOP_WORKOUT = "/stop_workout"
    
    /**
     * 워치에 운동 시작 메시지 전송
     * 
     * @param context Context
     * @param sessionId 서버에서 생성된 세션 ID
     * @return 전송 성공 여부
     */
    suspend fun sendStartWorkout(context: Context, sessionId: Long): Boolean {
        return try {
            val nodeClient = Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.await()
            
            if (nodes.isEmpty()) {
                Log.e(TAG, "❌ 연결된 워치가 없습니다")
                return false
            }
            
            val messageClient = Wearable.getMessageClient(context)
            val message = sessionId.toString().toByteArray()
            
            // 연결된 모든 노드에 전송
            var success = false
            nodes.forEach { node ->
                try {
                    messageClient.sendMessage(node.id, PATH_START_WORKOUT, message).await()
                    Log.d(TAG, "✅ 워치에 시작 메시지 전송 성공 (sessionId: $sessionId, node: ${node.displayName})")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 워치 메시지 전송 실패: ${e.message}")
                }
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ 워치 통신 오류: ${e.message}", e)
            false
        }
    }
    
    /**
     * 워치에 운동 중지 메시지 전송
     */
    suspend fun sendStopWorkout(context: Context): Boolean {
        return try {
            val nodeClient = Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.await()
            
            if (nodes.isEmpty()) {
                Log.e(TAG, "❌ 연결된 워치가 없습니다")
                return false
            }
            
            val messageClient = Wearable.getMessageClient(context)
            
            var success = false
            nodes.forEach { node ->
                try {
                    messageClient.sendMessage(node.id, PATH_STOP_WORKOUT, byteArrayOf()).await()
                    Log.d(TAG, "✅ 워치에 중지 메시지 전송 성공 (node: ${node.displayName})")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 워치 메시지 전송 실패: ${e.message}")
                }
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ 워치 통신 오류: ${e.message}", e)
            false
        }
    }
    
    /**
     * 워치에 데이터 동기화 요청 메시지 전송
     * (모바일 앱이 열렸을 때 호출 - 워치의 미동기화 데이터를 가져옴)
     */
    suspend fun requestSync(context: Context): Boolean {
        return try {
            val nodeClient = Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.await()
            
            if (nodes.isEmpty()) {
                Log.d(TAG, "연결된 워치가 없습니다")
                return false
            }
            
            val messageClient = Wearable.getMessageClient(context)
            
            var success = false
            nodes.forEach { node ->
                try {
                    messageClient.sendMessage(node.id, "/sync_request", byteArrayOf()).await()
                    Log.d(TAG, "✅ 워치에 동기화 요청 전송 (node: ${node.displayName})")
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "❌ 워치 메시지 전송 실패: ${e.message}")
                }
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ 워치 통신 오류: ${e.message}", e)
            false
        }
    }
    
    /**
     * 워치에 모바일 준비 완료 메시지 전송 (하위 호환성)
     * requestSync()와 동일한 동작
     */
    suspend fun sendMobileReady(context: Context): Boolean {
        return requestSync(context)
    }
}

