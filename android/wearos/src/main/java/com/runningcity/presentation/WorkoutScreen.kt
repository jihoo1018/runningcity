package com.runningcity.presentation

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.sharp.Bolt
import androidx.compose.material.icons.sharp.Pause
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material.icons.sharp.Speed
import androidx.compose.material.icons.sharp.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.local.entity.HeartRateRecordEntity
import com.runningcity.data.local.entity.WorkoutSessionEntity
import com.runningcity.data.sync.SyncResult
import com.runningcity.data.sync.WatchDataSyncRepository
import com.runningcity.presentation.component.InfoItem
import com.runningcity.presentation.component.RoundOutlineButton
import com.runningcity.presentation.theme.RunningcityTheme
import com.runningcity.presentation.theme.accentBlue
import com.runningcity.presentation.theme.accentGreen
import com.runningcity.presentation.theme.accentOrange
import com.runningcity.presentation.theme.accentRed
import com.runningcity.service.WorkoutService
import com.runningcity.utils.CsvExporter
import com.runningcity.utils.MobileCommunicationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun WorkoutScreen(
    context: Context,
    onWorkoutComplete: (Long) -> Unit
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
    var currentPace by remember { mutableStateOf(0) }  // ⭐ 페이스 추가

    var clientSecretKey by remember { mutableStateOf("") }
    var workoutSessionSeq by remember { mutableStateOf(0L) }
    var startTime by remember { mutableStateOf(0L) }

    var mobileSessionId by remember { mutableStateOf<Long?>(null) }

    // ✅ 거리를 km로 변환하는 함수
    fun formatDistance(meters: Float): String {
        val km = meters / 1000.0
        return String.format("%.2f", km)
    }

    // ⭐ 페이스 포맷팅 함수 추가
    fun formatPace(secondsPerKm: Int): String {
        if (secondsPerKm <= 0) return "0:00"
        val minutes = secondsPerKm / 60
        val seconds = secondsPerKm % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    // 화면 진입 시 자동 시작
    LaunchedEffect(Unit) {
        if (!isRunning) {
            isRunning = true
            println("🚀 WorkoutScreen 진입 - 자동 시작!")
        }
    }

    val database = remember { WorkoutDatabase.getDatabase(context) }
    val dao = database.workoutDao()
    val scope = rememberCoroutineScope()

    val heartRateList = remember { mutableListOf<HeartRateRecordEntity>() }

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    var workoutService by remember { mutableStateOf<WorkoutService?>(null) }
    var serviceBound by remember { mutableStateOf(false) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WorkoutService.WorkoutBinder
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

                workoutService?.onHeartRateUpdate = { hr ->
                    heartRate = hr
                }

                // ⭐ 페이스 콜백 추가
                workoutService?.onPaceUpdate = { pace ->
                    currentPace = pace
                    println("🏃 [WorkoutScreen] 페이스 업데이트: ${pace}초/km (${formatPace(pace)})")
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
                sessionId = mobileSessionId ?: 0L,
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
                sensorManager.registerListener(
                    heartRateListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                println("✅ 심박수 센서 등록")
            }

            val serviceIntent = Intent(context, WorkoutService::class.java).apply {
                action = WorkoutService.ACTION_START
            }
            context.startForegroundService(serviceIntent)

            context.bindService(
                Intent(context, WorkoutService::class.java),
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
                val finalPace = workoutService?.getCurrentPace() ?: 0  // ⭐ 페이스 추가

                val elevationList = workoutService?.getElevationList() ?: emptyList()

                val serviceIntent = Intent(context, WorkoutService::class.java).apply {
                    action = WorkoutService.ACTION_STOP
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

                // 독립적인 코루틴 스코프 사용 (Compose 재구성으로 인한 취소 방지)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // 데이터 전송을 위한 WatchDataSyncRepository 생성
                        val syncRepository = WatchDataSyncRepository(context, database)
                        
                        // 항상 데이터를 전송 시도 (Data Layer API는 큐에 저장되므로 모바일 앱이 꺼져있어도 안전)
                        println("📤 운동 데이터 전송 시작")
                        
                        val syncResult = syncRepository.syncWorkoutData(
                            watchSessionId = clientSecretKey,
                            batchSize = Int.MAX_VALUE // 모든 데이터 한 번에 전송
                        )
                        
                        when (syncResult) {
                            is SyncResult.Success -> {
                                println("✅ 운동 데이터 전송 성공: ${syncResult.cadenceCount}개 케이던스, ${syncResult.heartRateCount}개 심박수, ${syncResult.gpsCount}개 GPS")
                            }
                            is SyncResult.Queued -> {
                                println("📮 운동 데이터가 Data Layer 큐에 추가됨 (모바일 앱이 켜지면 자동 수신)")
                            }
                            is SyncResult.NoData -> {
                                println("📭 전송할 운동 데이터 없음")
                            }
                            is SyncResult.Error -> {
                                println("❌ 운동 데이터 전송 실패: ${syncResult.message}")
                            }
                        }
                        
                        // 모바일에 알림 전송
                        if (mobileSessionId != null) {
                            MobileCommunicationHelper.notifyWorkoutStopped(context, mobileSessionId!!)
                            println("📡 모바일에 운동 종료 알림 전송 (sessionId: $mobileSessionId)")
                        } else {
                            MobileCommunicationHelper.notifyDataReady(context)
                            println("📡 모바일에 동기화 요청 전송")
                        }
                    } catch (e: Exception) {
                        println("❌ 데이터 전송 중 예외 발생: ${e.message}")
                        e.printStackTrace()
                    }
                }
                println("⏹️ 운동 종료 - 데이터 DB 저장 완료 (세션: $clientSecretKey)")

                val endTime = System.currentTimeMillis()
                val duration = ((endTime - startTime) / 1000).toInt()

                val avgElevation = if (elevationList.isNotEmpty()) {
                    elevationList.average()
                } else {
                    0.0
                }

                val totalDurationSeconds = duration - finalPausedDuration.toInt()

                // ⭐ 페이스 계산: Service에서 가져온 값 우선 사용, 없으면 직접 계산
                val avgPace = if (finalPace > 0) {
                    finalPace
                } else if (finalDistance > 0 && totalDurationSeconds > 0) {
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
                println("   - 평균 페이스: ${avgPace}초/km (${avgPace / 60}분 ${avgPace % 60}초/km)")
                println("   - 평균 고도: ${String.format("%.1f", avgElevation)}m")
                println("   - 지속 시간: ${totalDurationSeconds}초")

                val savedSeq = workoutSessionSeq

                clientSecretKey = ""
                workoutSessionSeq = 0L
                mobileSessionId = null
                isPaused = false
                pausedDuration = 0L
                currentPace = 0  // ⭐ 페이스 초기화

                onWorkoutComplete(savedSeq)
            }
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    "com.runningcity.START_WORKOUT_FROM_MOBILE" -> {
                        val sessionId = intent.getLongExtra("sessionId", 0L)
                        if (sessionId > 0 && !isRunning) {
                            println("📨 모바일에서 시작 요청 수신 (세션: $sessionId)")
                            mobileSessionId = sessionId
                            isRunning = true
                        }
                    }

                    "com.runningcity.STOP_WORKOUT_FROM_MOBILE" -> {
                        if (isRunning) {
                            println("📨 모바일에서 중지 요청 수신")
                            isRunning = false
                        }
                    }
                    
                    "com.runningcity.PAUSE_WORKOUT_FROM_MOBILE" -> {
                        if (isRunning && !isPaused) {
                            println("📨 모바일에서 일시정지 요청 수신")
                            val intent = Intent(context, WorkoutService::class.java).apply {
                                action = WorkoutService.ACTION_PAUSE
                            }
                            context.startService(intent)
                            isPaused = true
                        }
                    }
                    
                    "com.runningcity.RESUME_WORKOUT_FROM_MOBILE" -> {
                        if (isRunning && isPaused) {
                            println("📨 모바일에서 재개 요청 수신")
                            val intent = Intent(context, WorkoutService::class.java).apply {
                                action = WorkoutService.ACTION_RESUME
                            }
                            context.startService(intent)
                            isPaused = false
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction("com.runningcity.START_WORKOUT_FROM_MOBILE")
            addAction("com.runningcity.STOP_WORKOUT_FROM_MOBILE")
            addAction("com.runningcity.PAUSE_WORKOUT_FROM_MOBILE")
            addAction("com.runningcity.RESUME_WORKOUT_FROM_MOBILE")
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

    DisposableEffect(Unit) {
        onDispose {
            if (isRunning) {
                sensorManager.unregisterListener(heartRateListener)

                val serviceIntent = Intent(context, WorkoutService::class.java).apply {
                    action = WorkoutService.ACTION_STOP
                }
                context.startService(serviceIntent)

                if (serviceBound) {
                    try {
                        context.unbindService(serviceConnection)
                    } catch (e: Exception) {
                    }
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

    RunningcityTheme {
        Scaffold(
            timeText = { TimeText() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Spacer(Modifier.height(6.dp))

                    if (isRunning) {
                        if (!isPaused) {
                            // 🏃 운동 중 화면
                            // ✅ 거리 표시 (큰 사이즈)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formatDistance(totalDistance),
                                    style = MaterialTheme.typography.display2,
                                    color = MaterialTheme.colors.onPrimary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "km",
                                    style = MaterialTheme.typography.caption1,
                                    color = MaterialTheme.colors.onBackground
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                InfoItem(
                                    icon = Icons.Outlined.FavoriteBorder,
                                    iconDesc = "심박수",
                                    iconColor = MaterialTheme.colors.accentRed,
                                    text = "$heartRate",
                                    title = "bpm"
                                )
                                InfoItem(
                                    icon = Icons.Outlined.Timer,
                                    iconDesc = "페이스",
                                    iconColor = MaterialTheme.colors.accentGreen,
                                    text = formatPace(currentPace),  // ⭐ 실시간 페이스 표시
                                    title = "/km"
                                )
                            }

                            RoundOutlineButton(
                                onClick = {
                                    val intent = Intent(context, WorkoutService::class.java).apply {
                                        action = WorkoutService.ACTION_PAUSE
                                    }
                                    context.startService(intent)
                                    isPaused = true
                                    println("⏸️ 운동 일시정지")
                                    
                                    // 워치에서 모바일로 일시정지 요청 전송
                                    scope.launch {
                                        MobileCommunicationHelper.notifyPauseRunning(context)
                                    }
                                },
                                icon = Icons.Sharp.Pause,
                                iconDesc = "일시정지",
                                border = true
                            )

                        } else {
                            // ⏸️ 일시정지 화면
                            // ✅ 거리 표시
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formatDistance(totalDistance),
                                    style = MaterialTheme.typography.display2,
                                    color = MaterialTheme.colors.onPrimary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "km",
                                    style = MaterialTheme.typography.caption1,
                                    color = MaterialTheme.colors.onBackground
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                InfoItem(
                                    icon = Icons.Outlined.FavoriteBorder,
                                    iconDesc = "심박수",
                                    iconColor = MaterialTheme.colors.accentRed,
                                    text = "$heartRate",
                                    title = "bpm"
                                )
                                InfoItem(
                                    icon = Icons.Outlined.Timelapse,
                                    iconDesc = "페이스",
                                    iconColor = MaterialTheme.colors.accentGreen,
                                    text = formatPace(currentPace),  // ⭐ 실시간 페이스 표시
                                    title = "/km"
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                InfoItem(
                                    icon = Icons.Sharp.Speed,
                                    iconDesc = "케이던스",
                                    iconColor = MaterialTheme.colors.accentBlue,
                                    text = "$currentCadence",
                                    title = "SPM"
                                )
                                InfoItem(
                                    icon = Icons.Sharp.Bolt,
                                    iconDesc = "칼로리",
                                    iconColor = MaterialTheme.colors.accentOrange,
                                    text = "${totalCalories.toInt()}",
                                    title = "kcal"
                                )
                            }

//                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RoundOutlineButton(
                                    onClick = {
                                        val intent = Intent(context, WorkoutService::class.java).apply {
                                            action = WorkoutService.ACTION_RESUME
                                        }
                                        context.startService(intent)
                                        isPaused = false
                                        println("▶️ 운동 재개")
                                        
                                        // 워치에서 모바일로 재개 요청 전송
                                        scope.launch {
                                            MobileCommunicationHelper.notifyResumeRunning(context)
                                        }
                                    },
                                    icon = Icons.Sharp.PlayArrow,
                                    iconDesc = "재개",
                                    border = true
                                )

                                RoundOutlineButton(
                                    onClick = {
                                        isRunning = false
                                        isPaused = false
                                        println("⏹️ 운동 종료")
                                        
                                        // 워치에서 모바일로 중단 요청 전송
                                        scope.launch {
                                            MobileCommunicationHelper.notifyStopRunning(context)
                                        }
                                    },
                                    icon = Icons.Sharp.Stop,
                                    iconDesc = "종료",
                                    iconColor = MaterialTheme.colors.background,
                                    bgColor = MaterialTheme.colors.onPrimary,
                                    border = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (serviceBound) {
                            Text(
                                text = "🔔 백그라운드 추적 중",
                                style = MaterialTheme.typography.caption2,
                                color = Color.Green
                            )
                        }

                        Text(
                            text = if (isPaused) "⏸️ 일시정지 중" else "🏃 측정 중...",
                            style = MaterialTheme.typography.caption2,
                            color = if (isPaused) Color.Yellow else Color.Green
                        )

                        // ⭐ 디버그: 페이스 값 확인용
                        if (currentPace > 0) {
                            Text(
                                text = "페이스: ${formatPace(currentPace)} (${currentPace}초/km)",
                                style = MaterialTheme.typography.caption2,
                                color = Color.Cyan
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
    }
}

//    Scaffold(
//        timeText = { TimeText() }
//    ) {
//        val scrollState = rememberScrollState()
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black)
//                .verticalScroll(scrollState)
//                .padding(8.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Top
//        ) {
//
//            Spacer(modifier = Modifier.height(20.dp))
//
//
//            Text(
//                text = "운동 측정",
//                style = MaterialTheme.typography.title3
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            InfoItem(
//                icon = Icons.Rounded.Favorite,
//                iconDesc = "심박수",
//                tint = MaterialTheme.colors.accentRed,
//                text = "$heartRate",
//                title = "심박수"
//            )
//
//            // 심박수
//            Text(
//                text = "💓 $heartRate",
//                style = MaterialTheme.typography.display2,
//                color = if (heartRate > 0) Color.Red else Color.Gray
//            )
//            Text(text = "bpm", style = MaterialTheme.typography.caption1)
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // 걸음수
//            Text(
//                text = "👟 $steps",
//                style = MaterialTheme.typography.display2,
//                color = if (steps > 0) Color.Green else Color.Gray
//            )
//            Text(text = "steps", style = MaterialTheme.typography.caption1)
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // 케이던스
//            Text(
//                text = "🏃 $currentCadence",
//                style = MaterialTheme.typography.display3,
//                color = if (currentCadence > 0) Color(0xFF9C27B0) else Color.Gray
//            )
//            Text(text = "spm", style = MaterialTheme.typography.caption1)
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // 거리
//            Text(
//                text = "📏 ${String.format("%.1f", totalDistance)}m",
//                style = MaterialTheme.typography.display3,
//                color = if (totalDistance > 0) Color.Cyan else Color.Gray
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // 칼로리
//            Text(
//                text = "🔥 ${String.format("%.1f", totalCalories)}",
//                style = MaterialTheme.typography.display3,
//                color = if (totalCalories > 0) Color(0xFFFF9800) else Color.Gray
//            )
//            Text(text = "kcal", style = MaterialTheme.typography.caption1)
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // GPS 정보
//            Text(
//                text = "📍 GPS (${gpsCount}개)",
//                style = MaterialTheme.typography.caption1
//            )
//            if (gpsAccuracy > 0) {
//                Text(
//                    text = "정확도: ${gpsAccuracy.toInt()}m",
//                    style = MaterialTheme.typography.caption2,
//                    color = Color.Yellow
//                )
//            }
//
//            if (serviceBound) {
//                Text(
//                    text = "🔔 백그라운드 추적 중",
//                    style = MaterialTheme.typography.caption2,
//                    color = Color.Green
//                )
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // 버튼
//            if (isRunning) {
//                Button(
//                    onClick = {
//                        if (isPaused) {
//                            val intent = Intent(context, WorkoutService::class.java).apply {
//                                action = WorkoutService.ACTION_RESUME
//                            }
//                            context.startService(intent)
//                            isPaused = false
//                            println("▶️ 운동 재개")
//                        } else {
//                            val intent = Intent(context, WorkoutService::class.java).apply {
//                                action = WorkoutService.ACTION_PAUSE
//                            }
//                            context.startService(intent)
//                            isPaused = true
//                            println("⏸️ 운동 일시정지")
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth(0.9f),
//                    colors = ButtonDefaults.secondaryButtonColors()
//                ) {
//                    Text(if (isPaused) "▶️ 재개" else "⏸️ 일시정지")
//                }
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Button(
//                    onClick = {
//                        isRunning = false
//                        isPaused = false
//                        println("⏹️ 운동 종료")
//                    },
//                    modifier = Modifier.fillMaxWidth(0.9f),
//                    colors = ButtonDefaults.buttonColors(
//                        backgroundColor = Color.Red
//                    )
//                ) {
//                    Text("⏹️ 종료")
//                }
//            } else {
//                Button(
//                    onClick = {
//                        isRunning = true
//                        isPaused = false
//                        println("▶️ 운동 시작")
//                    },
//                    modifier = Modifier.fillMaxWidth(0.9f),
//                    colors = ButtonDefaults.primaryButtonColors()
//                ) {
//                    Text("▶️ 시작")
//                }
//            }
//
//            if (isRunning) {
//                Spacer(modifier = Modifier.height(4.dp))
//
//                if (isPaused) {
//                    Text(
//                        text = "⏸️ 일시정지 중",
//                        style = MaterialTheme.typography.caption2,
//                        color = Color.Yellow
//                    )
//                } else {
//                    Text(
//                        text = "🏃 측정 중...",
//                        style = MaterialTheme.typography.caption2,
//                        color = Color.Green
//                    )
//                }
//
//                if (pausedDuration > 0) {
//                    Text(
//                        text = "⏸️ 누적: ${pausedDuration}초",
//                        style = MaterialTheme.typography.caption2,
//                        color = Color.Gray
//                    )
//                }
//
//                Text(
//                    text = "Key: $clientSecretKey",
//                    style = MaterialTheme.typography.caption2,
//                    color = Color.Gray
//                )
//                Text(
//                    text = "Seq: $workoutSessionSeq",
//                    style = MaterialTheme.typography.caption2,
//                    color = Color.Gray
//                )
//            }
//
//            Spacer(modifier = Modifier.height(20.dp))
//        }
//    }
//}