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
}

