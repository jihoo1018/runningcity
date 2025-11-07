package com.runningcity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.runningcity.data.WorkoutDataBatch
import com.runningcity.ui.theme.RunningcityTheme
import com.runningcity.utils.WatchCommunicationHelper
import kotlinx.coroutines.launch

/**
 * MainActivity
 * - 모바일에서 운동을 시작하는 메인 화면
 * - 시작 버튼을 누르면 세션 ID를 생성하고 워치에 전달
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Compose UI 표시
        setContent {
            RunningcityTheme {
                WorkoutControlScreen()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // 모바일 앱이 열렸을 때 워치에 준비 완료 알림
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            WatchCommunicationHelper.sendMobileReady(this@MainActivity)
        }
    }
}

/**
 * WorkoutControlScreen
 * - 운동 시작/중지 UI
 */
@Composable
fun WorkoutControlScreen() {
    var isRunning by remember { mutableStateOf(false) }
    var currentSessionId by remember { mutableStateOf<Long?>(null) }
    var statusMessage by remember { mutableStateOf("") }
    var isForeground by remember { mutableStateOf(true) }
    
    // 워치에서 받은 운동 데이터 저장 (백엔드로 전송할 데이터)
    var receivedWorkoutData by remember { mutableStateOf<WorkoutDataBatch?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gson = remember { Gson() }
    
    // 워치에서 메시지 수신
    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                when (intent?.action) {
                    "com.runningcity.WATCH_DATA_READY" -> {
                        // 모바일이 포그라운드 상태면 즉시 준비 완료 응답
                        if (isForeground) {
                            scope.launch {
                                WatchCommunicationHelper.sendMobileReady(context)
                            }
                        }
                    }
                    "com.runningcity.WORKOUT_STOPPED_FROM_WATCH" -> {
                        // 워치에서 운동 종료
                        val sessionId = intent.getLongExtra("sessionId", 0L)
                        if (sessionId > 0 && currentSessionId == sessionId) {
                            println("⏹️ 워치에서 운동 종료됨 (sessionId: $sessionId)")
                            isRunning = false
                            statusMessage = "워치에서 운동이 종료되었습니다"
                            currentSessionId = null
                        }
                    }
                    "com.runningcity.WORKOUT_DATA_RECEIVED" -> {
                        // 워치에서 운동 데이터 수신
                        val jsonString = intent.getStringExtra("workoutData")
                        if (jsonString != null) {
                            try {
                                val workoutData = gson.fromJson(jsonString, WorkoutDataBatch::class.java)
                                receivedWorkoutData = workoutData
                                println("✅ 운동 데이터 수신 및 저장 완료")
                                println("   - clientSecretKey: ${workoutData.clientSecretKey}")
                                println("   - sessionId: ${workoutData.sessionId}")
                                println("   - heartRateRecords: ${workoutData.heartRateRecords.size}개")
                                println("   - gpsPoints: ${workoutData.gpsPoints.size}개")
                                println("   - cadenceRecords: ${workoutData.cadenceRecords.size}개")
                                
                                // TODO: 여기서 백엔드로 데이터 전송
                                // sendToBackend(workoutData)
                                
                            } catch (e: Exception) {
                                println("❌ 운동 데이터 파싱 실패: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
        
        val filter = android.content.IntentFilter().apply {
            addAction("com.runningcity.WATCH_DATA_READY")
            addAction("com.runningcity.WORKOUT_STOPPED_FROM_WATCH")
            addAction("com.runningcity.WORKOUT_DATA_RECEIVED")
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    
    // 포그라운드/백그라운드 상태 추적
    DisposableEffect(Unit) {
        isForeground = true
        onDispose {
            isForeground = false
        }
    }

    // UI 구성
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Running City",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // 시작/중지 버튼
        Button(
            onClick = {
                scope.launch {
                    if (!isRunning) {
                        // 운동 시작
                        // TODO: 나중에 서버에서 세션 ID를 받아올 예정
                        val sessionId = System.currentTimeMillis() // 임시 세션 ID
                        
                        val success = WatchCommunicationHelper.sendStartWorkout(context, sessionId)
                        if (success) {
                            isRunning = true
                            currentSessionId = sessionId
                            statusMessage = "워치에서 운동 시작됨 (세션: $sessionId)"
                        } else {
                            statusMessage = "워치 연결 실패"
                        }
                    } else {
                        // 운동 중지
                        val success = WatchCommunicationHelper.sendStopWorkout(context)
                        if (success) {
                            isRunning = false
                            statusMessage = "워치에서 운동 중지됨"
                            currentSessionId = null
                        } else {
                            statusMessage = "워치 연결 실패"
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (isRunning) "운동 중지" else "운동 시작",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 상태 메시지
        if (statusMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // 세션 ID 표시
        if (currentSessionId != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "세션 ID: $currentSessionId",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 받은 운동 데이터 표시
        if (receivedWorkoutData != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 받은 운동 데이터",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    receivedWorkoutData?.let { data ->
                        Text(
                            text = "clientSecretKey: ${data.clientSecretKey}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "sessionId: ${data.sessionId ?: "null"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "심박수: ${data.heartRateRecords.size}개",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "GPS: ${data.gpsPoints.size}개",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "케이던스: ${data.cadenceRecords.size}개",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
