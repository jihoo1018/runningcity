package com.runningcity.presentation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.local.entity.HeartRateRecordEntity
import com.runningcity.data.local.entity.WorkoutSessionEntity
import com.runningcity.service.WorkoutService_backup
import com.runningcity.utils.CsvExporter
import com.runningcity.utils.MobileCommunicationHelper
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun WorkoutScreen(
    context: Context,
    onWorkoutComplete: (Long) -> Unit  // ✅ 1. 여기에 콜백 파라미터 추가!
) {
    var heartRate by remember { mutableStateOf(0) }
    var steps by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    var isPaused by remember { mutableStateOf(false) }
    var pausedDuration by remember { mutableStateOf(0L) }

    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var gpsAccuracy by remember { mutableStateOf(0f) }
    var gpsCount by remember { mutableStateOf(0) }
    var totalDistance by remember { mutableStateOf(0f) }
    var totalCalories by remember { mutableStateOf(0.0) }

    var currentCadence by remember { mutableStateOf(0) }

    var clientSecretKey by remember { mutableStateOf("") }
    var workoutSessionSeq by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(0L) }
    
    // 모바일에서 시작된 세션 ID
    var mobileSessionId by remember { mutableStateOf<Long?>(null) }

    val database = remember { WorkoutDatabase.getDatabase(context) }
    val dao = database.workoutDao()
    val scope = rememberCoroutineScope()

    val heartRateList = remember { mutableListOf<HeartRateRecordEntity>() }

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    var workoutService by remember { mutableStateOf<WorkoutService_backup?>(null) }
    var serviceBound by remember { mutableStateOf(false) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WorkoutService_backup.WorkoutBinder
                workoutService = binder.getService()
                serviceBound = true
                println("✅ Service 연결됨")

                workoutService?.onLocationUpdate = { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                    gpsAccuracy = location.accuracy
                    gpsCount++
                }

                workoutService?.onDistanceUpdate = { distance ->
                    totalDistance = distance
                }

                workoutService?.onCalorieUpdate = { calories ->
                    totalCalories = calories
                }

                workoutService?.onCadenceUpdate = { cadence ->
                    currentCadence = cadence
                }

                workoutService?.onStepsUpdate = { serviceSteps ->
                    steps = serviceSteps
                }

                isPaused = workoutService?.isPaused() ?: false
                pausedDuration = workoutService?.getTotalPausedDuration() ?: 0L
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                workoutService = null
                serviceBound = false
                println("❌ Service 연결 해제됨")
            }
        }
    }

    val heartRateSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    }

    val heartRateListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                heartRate = event.values[0].toInt()

                if (isRunning && !isPaused && workoutSessionSeq != 0L) {
                    val record = HeartRateRecordEntity(
                        workoutSessionSeq = workoutSessionSeq,
                        createdAt = System.currentTimeMillis(),
                        heartRate = heartRate,
                        syncedToServer = false,
                        savedAt = System.currentTimeMillis()
                    )
                    heartRateList.add(record)
                    scope.launch {
                        dao.insertHeartRate(record)
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            println("🚀 운동 시작!")

            clientSecretKey = "run_${UUID.randomUUID().toString().substring(0, 8)}"
            startTime = System.currentTimeMillis()

            val session = WorkoutSessionEntity(
                clientSecretKey = clientSecretKey,
                sessionId = mobileSessionId ?: 0L,  // 모바일에서 시작한 경우 sessionId 설정
                startTime = startTime,
                status = "IN_PROGRESS"
            )
            workoutSessionSeq = dao.insertSession(session)

            if (mobileSessionId != null) {
                println("💾 모바일 세션 저장 완료: seq=$workoutSessionSeq, key=$clientSecretKey, mobileSessionId=$mobileSessionId")
            } else {
                println("💾 워치 세션 저장 완료: seq=$workoutSessionSeq, key=$clientSecretKey")
            }

            heartRateList.clear()
            gpsCount = 0

            heartRateSensor?.let {
                sensorManager.registerListener(heartRateListener, it, SensorManager.SENSOR_DELAY_NORMAL)
                println("✅ 심박수 센서 등록")
            }

            val serviceIntent = Intent(context, WorkoutService_backup::class.java).apply {
                action = WorkoutService_backup.ACTION_START
            }
            context.startForegroundService(serviceIntent)

            context.bindService(
                Intent(context, WorkoutService_backup::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )

            kotlinx.coroutines.delay(500)
            workoutService?.clientSecretKey = clientSecretKey
            workoutService?.workoutSessionSeq = workoutSessionSeq


        } else {
            // 운동 종료
            if (clientSecretKey.isNotEmpty()) {
                println("⏹️ 운동 종료!")

                sensorManager.unregisterListener(heartRateListener)

                val finalDistance = workoutService?.getTotalDistance() ?: 0f
                val finalPausedDuration = workoutService?.getTotalPausedDuration() ?: 0L
                val finalCalories = workoutService?.getTotalCalories() ?: 0.0
                val finalSteps = workoutService?.getTotalSteps() ?: steps
                val finalCadence = workoutService?.getCurrentCadence() ?: 0

                val elevationList = workoutService?.getElevationList() ?: emptyList()

                val serviceIntent = Intent(context, WorkoutService_backup::class.java).apply {
                    action = WorkoutService_backup.ACTION_STOP
                }
                context.startService(serviceIntent)

                if (serviceBound) {
                    try {
                        context.unbindService(serviceConnection)
                        serviceBound = false
                    } catch (e: Exception) {
                        println("⚠️ Service 언바인딩 실패: ${e.message}")
                    }
                }

    //////////////////////////////////////////////////////////////
    // 워치 -> 모바일 데이터 전달하기 위해 추가한 부분
                // ⏹️ 운동 세션 중지 - DB에 저장 + 모바일에 알림
                scope.launch {
                    if (mobileSessionId != null) {
                        // 모바일에서 시작한 경우 - 운동 종료 알림
                        MobileCommunicationHelper.notifyWorkoutStopped(context, mobileSessionId!!)
                        println("📡 모바일에 운동 종료 알림 전송 (sessionId: $mobileSessionId)")
                    } else {
                        // 워치에서 시작한 경우 - 데이터 준비 알림
                        MobileCommunicationHelper.notifyDataReady(context)
                        println("📡 모바일에 동기화 요청 전송")
                    }
                }
                println("⏹️ 운동 종료 - 데이터 DB 저장 완료 (세션: $clientSecretKey)")
    //////////////////////////////////////////////////////////////

                val endTime = System.currentTimeMillis()
                val duration = ((endTime - startTime) / 1000).toInt()

                val avgElevation = if (elevationList.isNotEmpty()) {
                    elevationList.average()
                } else {
                    0.0
                }

                val totalDurationSeconds = duration - finalPausedDuration.toInt()
                val avgPace = if (finalDistance > 0 && totalDurationSeconds > 0) {
                    val avgSpeed = finalDistance / totalDurationSeconds.toDouble()
                    (1000.0 / avgSpeed).toInt()
                } else {
                    0
                }

                val avgHr = dao.getAvgHeartRate(workoutSessionSeq) ?: 0
                val maxHr = dao.getMaxHeartRate(workoutSessionSeq) ?: 0
                val avgCadence = dao.getAvgCadence(workoutSessionSeq) ?: finalCadence

                val locations = dao.getLocations(workoutSessionSeq)
                val cadences = dao.getCadences(workoutSessionSeq)
                val calories = dao.getCalories(workoutSessionSeq)

                val updatedSession = WorkoutSessionEntity(
                    seq = workoutSessionSeq,
                    clientSecretKey = clientSecretKey,
                    startTime = startTime,
                    endTime = endTime,
                    duration = totalDurationSeconds,
                    status = "COMPLETED",
                    totalSteps = finalSteps,
                    totalDistance = finalDistance.toDouble(),
                    totalCalories = finalCalories.toInt(),
                    avgHeartRate = avgHr,
                    avgCadence = avgCadence,
                    avgPace = avgPace,
                    elevation = avgElevation,
                    createdAt = startTime,
                    updatedAt = System.currentTimeMillis()
                )

                dao.updateSession(updatedSession)

                CsvExporter.exportAll(
                    context = context,
                    session = updatedSession,
                    heartRates = heartRateList,
                    locations = locations,
                    cadences = cadences,
                    calories = calories
                )

                println("✅ 데이터 저장 완료!")
                println("📊 요약:")
                println("   - 거리: ${finalDistance}m")
                println("   - 칼로리: ${String.format("%.1f", finalCalories)}kcal")
                println("   - 걸음: ${finalSteps}걸음")
                println("   - 평균 케이던스: ${avgCadence}spm")
                println("   - 평균 페이스: ${avgPace}초/km (${avgPace/60}분 ${avgPace%60}초/km)")
                println("   - 평균 고도: ${String.format("%.1f", avgElevation)}m")
                println("   - 지속 시간: ${totalDurationSeconds}초")

                // ✅ 2. seq 저장 후 초기화 전에 콜백 호출!
                val savedSeq = workoutSessionSeq

                clientSecretKey = ""
                workoutSessionSeq = 0L
                mobileSessionId = null
                isPaused = false
                pausedDuration = 0L

                // ✅ 3. 결과 화면으로 이동!
                onWorkoutComplete(savedSeq)
            }
        }
    }

    //////////////////////////////////////////////////////////////
    // 모바일 -> 워치 데이터 전달하기 위해 추가한 부분
    // 📡 모바일에서 운동 시작/중지 메시지 수신
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    "com.runningcity.START_WORKOUT_FROM_MOBILE" -> {
                        val sessionId = intent.getLongExtra("sessionId", 0L)
                        if (sessionId > 0 && !isRunning) {
                            println("📨 모바일에서 시작 요청 수신 (세션: $sessionId)")
                            mobileSessionId = sessionId
                            // 운동 시작 트리거
                            isRunning = true
                        }
                    }
                    "com.runningcity.STOP_WORKOUT_FROM_MOBILE" -> {
                        if (isRunning) {
                            println("📨 모바일에서 중지 요청 수신")
                            // 운동 중지 트리거
                            isRunning = false
                        }
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction("com.runningcity.START_WORKOUT_FROM_MOBILE")
            addAction("com.runningcity.STOP_WORKOUT_FROM_MOBILE")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    //////////////////////////////////////////////////////////////

    // 🔥 화면 종료 시 정리 (별도 DisposableEffect)
    DisposableEffect(Unit) {
        onDispose {
            if (isRunning) {
                sensorManager.unregisterListener(heartRateListener)

                val serviceIntent = Intent(context, WorkoutService_backup::class.java).apply {
                    action = WorkoutService_backup.ACTION_STOP
                }
                context.startService(serviceIntent)

                if (serviceBound) {
                    try {
                        context.unbindService(serviceConnection)
                    } catch (e: Exception) {}
                }
            }
        }
    }

    LaunchedEffect(isPaused, isRunning) {
        if (isPaused && isRunning) {
            while (isPaused) {
                kotlinx.coroutines.delay(1000)
                pausedDuration = workoutService?.getTotalPausedDuration() ?: 0L
            }
        }
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(scrollState)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "운동 측정",
                style = MaterialTheme.typography.title3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 심박수
            Text(
                text = "💓 $heartRate",
                style = MaterialTheme.typography.display2,
                color = if (heartRate > 0) Color.Red else Color.Gray
            )
            Text(text = "bpm", style = MaterialTheme.typography.caption1)

            Spacer(modifier = Modifier.height(8.dp))

            // 걸음수
            Text(
                text = "👟 $steps",
                style = MaterialTheme.typography.display2,
                color = if (steps > 0) Color.Green else Color.Gray
            )
            Text(text = "steps", style = MaterialTheme.typography.caption1)

            Spacer(modifier = Modifier.height(8.dp))

            // 케이던스
            Text(
                text = "🏃 $currentCadence",
                style = MaterialTheme.typography.display3,
                color = if (currentCadence > 0) Color(0xFF9C27B0) else Color.Gray
            )
            Text(text = "spm", style = MaterialTheme.typography.caption1)

            Spacer(modifier = Modifier.height(8.dp))

            // 거리
            Text(
                text = "📏 ${String.format("%.1f", totalDistance)}m",
                style = MaterialTheme.typography.display3,
                color = if (totalDistance > 0) Color.Cyan else Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 칼로리
            Text(
                text = "🔥 ${String.format("%.1f", totalCalories)}",
                style = MaterialTheme.typography.display3,
                color = if (totalCalories > 0) Color(0xFFFF9800) else Color.Gray
            )
            Text(text = "kcal", style = MaterialTheme.typography.caption1)

            Spacer(modifier = Modifier.height(8.dp))

            // GPS 정보
            Text(
                text = "📍 GPS (${gpsCount}개)",
                style = MaterialTheme.typography.caption1
            )
            if (gpsAccuracy > 0) {
                Text(
                    text = "정확도: ${gpsAccuracy.toInt()}m",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Yellow
                )
            }

            if (serviceBound) {
                Text(
                    text = "🔔 백그라운드 추적 중",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Green
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 버튼
            if (isRunning) {
                Button(
                    onClick = {
                        if (isPaused) {
                            val intent = Intent(context, WorkoutService_backup::class.java).apply {
                                action = WorkoutService_backup.ACTION_RESUME
                            }
                            context.startService(intent)
                            isPaused = false
                            println("▶️ 운동 재개")
                        } else {
                            val intent = Intent(context, WorkoutService_backup::class.java).apply {
                                action = WorkoutService_backup.ACTION_PAUSE
                            }
                            context.startService(intent)
                            isPaused = true
                            println("⏸️ 운동 일시정지")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = ButtonDefaults.secondaryButtonColors()
                ) {
                    Text(if (isPaused) "▶️ 재개" else "⏸️ 일시정지")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        isRunning = false
                        isPaused = false
                        println("⏹️ 운동 종료")
                    },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red
                    )
                ) {
                    Text("⏹️ 종료")
                }
            } else {
                Button(
                    onClick = {
                        isRunning = true
                        isPaused = false
                        println("▶️ 운동 시작")
                    },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Text("▶️ 시작")
                }
            }

            if (isRunning) {
                Spacer(modifier = Modifier.height(4.dp))

                if (isPaused) {
                    Text(
                        text = "⏸️ 일시정지 중",
                        style = MaterialTheme.typography.caption2,
                        color = Color.Yellow
                    )
                } else {
                    Text(
                        text = "🏃 측정 중...",
                        style = MaterialTheme.typography.caption2,
                        color = Color.Green
                    )
                }

                if (pausedDuration > 0) {
                    Text(
                        text = "⏸️ 누적: ${pausedDuration}초",
                        style = MaterialTheme.typography.caption2,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "Key: $clientSecretKey",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Gray
                )
                Text(
                    text = "Seq: $workoutSessionSeq",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}