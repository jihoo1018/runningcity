package com.runningcity.service

import android.util.Log
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.runningcity.data.WorkoutDataBatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// 워치 -> 모바일 데이터 전달하기 위해 추가한 부분
/**
 * WatchDataListenerService
 * 
 * 워치로부터 Data Layer API를 통해 전송되는 데이터를 수신하는 서비스
 * 
 * 주요 기능:
 * 1. 워치에서 보낸 운동 데이터(심박수, GPS, 케이던스, 칼로리) 수신
 * 2. JSON 형태로 로그 출력 (디버깅 및 모니터링용)
 * 
 * Data Layer API 동작 방식:
 * - 워치가 데이터를 putDataItem()으로 전송
 * - 모바일이 꺼져있으면 자동으로 큐에 쌓임
 * - 모바일이 켜지면 onDataChanged()가 자동 호출됨
 */
class WatchDataListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Gson 인스턴스 (JSON 변환용, Pretty Print 활성화)
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    companion object {
        private const val TAG = "WatchDataListener"
        
        // Data Layer 경로 정의
        private const val PATH_WORKOUT_DATA = "/workout_data"
        private const val PATH_SESSION_START = "/session_start"
        private const val PATH_SESSION_END = "/session_end"
        private const val PATH_WATCH_DATA_READY = "/watch_data_ready"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "✅ WatchDataListenerService 시작됨")
    }

    /**
     * 워치로부터 데이터가 도착했을 때 호출됨
     * 
     * ⚠️ 중요: DataEventBuffer는 release() 후 접근 불가
     * 따라서 버퍼가 닫히기 전에 모든 DataMap을 추출해야 함
     * 
     * @param dataEvents 변경된 DataItem들의 버퍼
     */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "📦 데이터 수신됨 - 이벤트 개수: ${dataEvents.count}")
        
        // ✅ 버퍼가 닫히기 전에 모든 데이터를 추출
        val extractedDataList = mutableListOf<ExtractedData>()
        
        dataEvents.forEach { event ->
            when (event.type) {
                DataEvent.TYPE_CHANGED -> {
                    try {
                        val dataItem = event.dataItem
                        val path = dataItem.uri.path ?: return@forEach
                        
                        Log.d(TAG, """
                            📩 새 데이터 도착
                            Path: $path
                            URI: ${dataItem.uri}
                        """.trimIndent())
                        
                        // 🔑 버퍼가 열려있는 동안 DataMap 추출 (Buffer closed 에러 방지)
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                        extractedDataList.add(ExtractedData(path, dataMap))
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ 데이터 추출 실패: ${e.message}", e)
                    }
                }
                DataEvent.TYPE_DELETED -> {
                    Log.d(TAG, "🗑️ 데이터 삭제됨: ${event.dataItem.uri.path}")
                }
            }
        }
        
        dataEvents.release() // ✅ 이제 버퍼를 닫아도 안전
        
        // 추출한 데이터를 비동기로 처리
        extractedDataList.forEach { data ->
            serviceScope.launch {
                processExtractedData(data)
            }
        }
    }

    /**
     * 추출된 데이터를 담는 데이터 클래스
     */
    private data class ExtractedData(
        val path: String,
        val dataMap: DataMap
    )

    /**
     * 추출된 데이터를 경로에 따라 처리
     */
    private suspend fun processExtractedData(data: ExtractedData) {
        when {
            data.path.startsWith(PATH_WORKOUT_DATA) -> {
                handleWorkoutData(data.dataMap)
            }
            data.path.startsWith(PATH_SESSION_START) -> {
                handleSessionStart(data.dataMap)
            }
            data.path.startsWith(PATH_SESSION_END) -> {
                handleSessionEnd(data.dataMap)
            }
            else -> {
                Log.w(TAG, "⚠️ 알 수 없는 경로: ${data.path}")
            }
        }
    }

    /**
     * 운동 데이터 처리 (심박수, GPS, 케이던스, 칼로리 배치)
     */
    private suspend fun handleWorkoutData(dataMap: DataMap) {
        try {
            val jsonString = dataMap.getString("json") ?: return
            
            // JSON 파싱
            val workoutBatch = gson.fromJson(jsonString, WorkoutDataBatch::class.java)
            
            // 📊 JSON 로그 출력
            logWorkoutDataAsJson(workoutBatch, jsonString)
            
            // TODO: PostgreSQL 저장 로직 추가
            // saveToPostgreSQL(workoutBatch)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 데이터 처리 실패: ${e.message}", e)
        }
    }

    /**
     * 세션 시작 이벤트 처리
     */
    private suspend fun handleSessionStart(dataMap: DataMap) {
        try {
            val sessionId = dataMap.getString("sessionId")
            val userId = dataMap.getString("userId")
            val startTime = dataMap.getLong("startTime")
            
            val json = gson.toJson(mapOf(
                "event" to "SESSION_START",
                "sessionId" to sessionId,
                "userId" to userId,
                "startTime" to startTime
            ))
            
            Log.d(TAG, """
                🏃 세션 시작
                $json
            """.trimIndent())
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 세션 시작 처리 실패: ${e.message}", e)
        }
    }

    /**
     * 세션 종료 이벤트 처리
     */
    private suspend fun handleSessionEnd(dataMap: DataMap) {
        try {
            val sessionId = dataMap.getString("sessionId")
            val endTime = dataMap.getLong("endTime")
            
            val json = gson.toJson(mapOf(
                "event" to "SESSION_END",
                "sessionId" to sessionId,
                "endTime" to endTime
            ))
            
            Log.d(TAG, """
                🏁 세션 종료
                $json
            """.trimIndent())
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 세션 종료 처리 실패: ${e.message}", e)
        }
    }

    /**
     * 📊 운동 데이터를 JSON 형태로 로그 출력
     */
    private fun logWorkoutDataAsJson(batch: WorkoutDataBatch, rawJson: String) {
        // Pretty Print JSON 로그
        Log.d(TAG, gson.toJson(batch))
    }

    /**
     * 워치 연결/해제 상태 감지
     */
    override fun onPeerConnected(peer: Node) {
        Log.d(TAG, "✅ 워치 연결됨: ${peer.displayName} (${peer.id})")
    }

    override fun onPeerDisconnected(peer: Node) {
        Log.d(TAG, "❌ 워치 연결 해제됨: ${peer.displayName} (${peer.id})")
    }

    /**
     * 워치로부터 메시지 수신 (MessageClient 사용 시)
     */
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, """
            📨 메시지 수신
            Path: ${messageEvent.path}
            Size: ${messageEvent.data.size} bytes
        """.trimIndent())
        
        when (messageEvent.path) {
            PATH_WATCH_DATA_READY -> {
                Log.d(TAG, "📡 워치에서 데이터 준비 완료 알림 수신")
                // 앱에 브로드캐스트 전송
                val intent = android.content.Intent("com.runningcity.WATCH_DATA_READY")
                sendBroadcast(intent)
            }
            "/ping" -> {
                Log.d(TAG, "🏓 Ping 수신 - 워치 연결 확인됨")
            }
            else -> {
                val message = String(messageEvent.data, Charsets.UTF_8)
                Log.d(TAG, "메시지 내용: $message")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "❌ WatchDataListenerService 종료됨")
    }
}

