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
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

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
    
    // OkHttp 클라이언트 (백엔드 API 호출용)
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // 백엔드 API URL (개발 환경)
    private val BASE_URL = "http://:8080/api/v1" // IP 입력
    // private val BASE_URL = "http://localhost:8080/api/v1" // 실제 기기용 (필요시 변경)

    companion object {
        private const val TAG = "WatchDataListener"
        
        // Data Layer 경로 정의
        private const val PATH_WORKOUT_DATA = "/workout_data"
        private const val PATH_SESSION_START = "/session_start"
        private const val PATH_SESSION_END = "/session_end"
        private const val PATH_WATCH_DATA_READY = "/watch_data_ready"
        private const val PATH_WORKOUT_STOPPED = "/workout_stopped"
        private const val PATH_HEART_RATE_MEASURED = "/heart_rate_measured"
        private const val PATH_HEART_RATE_ERROR = "/heart_rate_error"
        private const val PATH_WATCH_PAIRED = "/watch_paired"
        private const val PATH_WATCH_START_RUNNING = "/watch_start_running"
        private const val PATH_WATCH_PAUSE_RUNNING = "/watch_pause_running"
        private const val PATH_WATCH_RESUME_RUNNING = "/watch_resume_running"
        private const val PATH_WATCH_STOP_RUNNING = "/watch_stop_running"
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
            
            // ✅ SharedPreferences에 저장 (앱이 꺼져있을 때를 대비)
            val prefs = getSharedPreferences("watch_data", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putString("pending_workout_data", jsonString)
                .putLong("pending_workout_timestamp", System.currentTimeMillis())
                .apply()
            
            Log.d(TAG, "✅ 워치 데이터를 SharedPreferences에 저장 완료")
            
            // 앱에 브로드캐스트 전송 (앱이 켜져있을 때 즉시 전달)
            val intent = android.content.Intent("com.runningcity.WORKOUT_DATA_RECEIVED")
            intent.putExtra("workoutData", jsonString)  // JSON 문자열로 전달
            sendBroadcast(intent)
            
            Log.d(TAG, "✅ 운동 데이터 브로드캐스트 전송 완료")
            
            // ✅ 백엔드 DB에 먼저 저장
            saveToBackend(workoutBatch)
            
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
     * 백엔드 DB에 워치 데이터 저장
     */
    private suspend fun saveToBackend(batch: WorkoutDataBatch) {
        withContext(Dispatchers.IO) {
            try {
                // userId를 Long으로 변환
                val userId = batch.userId.toLongOrNull() ?: run {
                    Log.e(TAG, "❌ userId 변환 실패: ${batch.userId}")
                    return@withContext
                }
                
                // WorkoutDataBatch를 WatchUploadRequest 형식으로 변환
                val requestBody = createWatchUploadRequest(batch)
                val requestJson = gson.toJson(requestBody)
                
                Log.d(TAG, "📤 백엔드에 워치 데이터 저장 요청")
                Log.d(TAG, "   userId: $userId")
                Log.d(TAG, "   clientSecretKey: ${batch.clientSecretKey}")
                
                // POST 요청 생성
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("$BASE_URL/sessions/watch?userId=$userId")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                // 요청 실행
                val response = okHttpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "✅ 백엔드 저장 성공")
                    Log.d(TAG, "   응답: $responseBody")
                    
                    // 응답 파싱 (선택사항)
                    try {
                        val responseJson = gson.fromJson(responseBody, Map::class.java)
                        val status = responseJson["status"] as? Number
                        val code = responseJson["code"] as? String
                        if (status?.toInt() == 200 && code == "COMMON_2000") {
                            Log.d(TAG, "✅ 세션이 성공적으로 DB에 저장되었습니다")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ 응답 파싱 실패 (무시 가능): ${e.message}")
                    }
                } else {
                    Log.e(TAG, "❌ 백엔드 저장 실패: ${response.code} - ${response.message}")
                    val errorBody = response.body?.string()
                    Log.e(TAG, "   에러 본문: $errorBody")
                }
                
                response.close()
            } catch (e: Exception) {
                Log.e(TAG, "❌ 백엔드 저장 중 예외 발생: ${e.message}", e)
            }
        }
    }
    
    /**
     * WorkoutDataBatch를 WatchUploadRequest 형식으로 변환
     */
    private fun createWatchUploadRequest(batch: WorkoutDataBatch): Map<String, Any> {
        return mapOf(
            "clientSecretKey" to batch.clientSecretKey,
            "startTime" to batch.startTime,
            "endTime" to batch.endTime,
            "summary" to mapOf(
                "totalSteps" to batch.summary.totalSteps,
                "totalDistance" to batch.summary.totalDistance,
                "totalCalories" to batch.summary.totalCalories,
                "avgHeartRate" to batch.summary.avgHeartRate,
                "duration" to batch.summary.duration,
                "avgCadence" to batch.summary.avgCadence,
                "avgPace" to batch.summary.avgPace,
                "elevation" to batch.summary.elevation
            ),
            "cadenceRecords" to batch.cadenceRecords.map { mapOf(
                "seq" to it.seq.toInt(),
                "cadence" to it.cadence,
                "createdAt" to it.createdAt
            ) },
            "heartRateRecords" to batch.heartRateRecords.map { mapOf(
                "seq" to it.seq.toInt(),
                "heartRate" to it.heartRate,
                "createdAt" to it.createdAt
            ) },
            "gpsPoints" to batch.gpsPoints.map { mapOf(
                "seq" to it.seq.toInt(),
                "latitude" to it.latitude,
                "longitude" to it.longitude,
                "altitude" to (it.altitude ?: 0.0),
                "speed" to (it.speed?.toDouble() ?: 0.0),
                "createdAt" to it.createdAt
            ) }
        )
    }

    /**
     * 워치 연결/해제 상태 감지
     */
    override fun onPeerConnected(peer: Node) {
        Log.d(TAG, "✅ 워치 연결됨: ${peer.displayName} (${peer.id})")
        // 워치 연결 시 동기화 요청 (워치에 저장된 미동기화 데이터 전송 요청)
        serviceScope.launch {
            try {
                kotlinx.coroutines.delay(1000) // 연결 안정화 대기
                val messageClient = Wearable.getMessageClient(this@WatchDataListenerService)
                messageClient.sendMessage(peer.id, "/sync_request", byteArrayOf()).await()
                Log.d(TAG, "✅ 워치에 동기화 요청 전송")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 동기화 요청 실패: ${e.message}", e)
            }
        }
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
                // 워치에 동기화 요청 메시지 전송 (워치가 데이터를 다시 전송하도록)
                serviceScope.launch {
                    try {
                        val nodeClient = Wearable.getNodeClient(this@WatchDataListenerService)
                        val messageClient = Wearable.getMessageClient(this@WatchDataListenerService)
                        val nodes = nodeClient.connectedNodes.await()
                        
                        nodes.forEach { node ->
                            try {
                                messageClient.sendMessage(node.id, "/sync_request", byteArrayOf()).await()
                                Log.d(TAG, "✅ 워치에 동기화 요청 전송 (node: ${node.displayName})")
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ 워치 메시지 전송 실패: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ 동기화 요청 실패: ${e.message}", e)
                    }
                }
                // 앱에 브로드캐스트 전송
                val intent = android.content.Intent("com.runningcity.WATCH_DATA_READY")
                sendBroadcast(intent)
            }
            PATH_WORKOUT_STOPPED -> {
                // 워치에서 운동 종료 알림
                val sessionIdStr = String(messageEvent.data, Charsets.UTF_8)
                val sessionId = sessionIdStr.toLongOrNull()
                
                if (sessionId != null) {
                    Log.d(TAG, "⏹️ 워치에서 운동 종료 (sessionId: $sessionId)")
                    // 앱에 브로드캐스트 전송
                    val intent = android.content.Intent("com.runningcity.WORKOUT_STOPPED_FROM_WATCH")
                    intent.putExtra("sessionId", sessionId)
                    sendBroadcast(intent)
                    
                    // 데이터 동기화 요청
                    val syncIntent = android.content.Intent("com.runningcity.WATCH_DATA_READY")
                    sendBroadcast(syncIntent)
                } else {
                    Log.e(TAG, "❌ sessionId 파싱 실패: $sessionIdStr")
                }
            }
            PATH_HEART_RATE_MEASURED -> {
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "💓 [3단계] 워치로부터 심박수 측정 결과 수신!")
                Log.d(TAG, "   📥 원본 데이터: ${String(messageEvent.data, Charsets.UTF_8)}")
                
                val heartRateStr = String(messageEvent.data, Charsets.UTF_8)
                val heartRate = heartRateStr.toIntOrNull()
                
                if (heartRate != null) {
                    Log.d(TAG, "✅ [4단계] 심박수 파싱 성공: $heartRate bpm")
                    Log.d(TAG, "   📤 WebAppInterface로 브로드캐스트 전송 중...")
                    
                    // 브로드캐스트로 WebAppInterface에 전달
                    val intent = android.content.Intent("com.runningcity.HEART_RATE_MEASURED")
                    intent.putExtra("heartRate", heartRate)
                    sendBroadcast(intent)
                    
                    Log.d(TAG, "✅ [5단계] 브로드캐스트 전송 완료")
                    Log.d(TAG, "   → WebAppInterface가 React로 전달할 예정")
                    Log.d(TAG, "═══════════════════════════════════════")
                } else {
                    Log.e(TAG, "❌ [실패] 심박수 파싱 실패")
                    Log.e(TAG, "   원본 문자열: '$heartRateStr'")
                    Log.e(TAG, "   → 정수로 변환할 수 없습니다")
                }
            }
            
            PATH_HEART_RATE_ERROR -> {
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "❌ [에러] 워치로부터 심박수 측정 에러 수신!")
                val errorMessage = String(messageEvent.data, Charsets.UTF_8)
                Log.e(TAG, "   📥 에러 메시지: $errorMessage")
                Log.e(TAG, "   📤 WebAppInterface로 브로드캐스트 전송 중...")
                
                // 브로드캐스트로 WebAppInterface에 전달
                val intent = android.content.Intent("com.runningcity.HEART_RATE_ERROR")
                intent.putExtra("errorMessage", errorMessage)
                sendBroadcast(intent)
                
                Log.e(TAG, "✅ 브로드캐스트 전송 완료")
                Log.e(TAG, "═══════════════════════════════════════")
            }
            
            PATH_WATCH_PAIRED -> {
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "⌚ [연동 완료] 워치로부터 연동 완료 수신!")
                Log.d(TAG, "   📤 WebAppInterface로 브로드캐스트 전송 중...")
                
                // 브로드캐스트로 WebAppInterface에 전달
                val intent = android.content.Intent("com.runningcity.WATCH_PAIRED")
                sendBroadcast(intent)
                
                Log.d(TAG, "✅ 브로드캐스트 전송 완료")
                Log.d(TAG, "═══════════════════════════════════════")
            }
            
            PATH_WATCH_START_RUNNING -> {
                Log.d(TAG, "🏃 워치에서 러닝 시작 요청 수신")
                // 앱에 브로드캐스트 전송
                val intent = android.content.Intent("com.runningcity.WATCH_START_RUNNING")
                sendBroadcast(intent)
            }
            PATH_WATCH_PAUSE_RUNNING -> {
                Log.d(TAG, "⏸️ 워치에서 러닝 일시정지 요청 수신")
                // 앱에 브로드캐스트 전송
                val intent = android.content.Intent("com.runningcity.WATCH_PAUSE_RUNNING")
                sendBroadcast(intent)
            }
            PATH_WATCH_RESUME_RUNNING -> {
                Log.d(TAG, "▶️ 워치에서 러닝 재개 요청 수신")
                // 앱에 브로드캐스트 전송
                val intent = android.content.Intent("com.runningcity.WATCH_RESUME_RUNNING")
                sendBroadcast(intent)
            }
            PATH_WATCH_STOP_RUNNING -> {
                Log.d(TAG, "⏹️ 워치에서 러닝 중단 요청 수신")
                // 앱에 브로드캐스트 전송
                val intent = android.content.Intent("com.runningcity.WATCH_STOP_RUNNING")
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

