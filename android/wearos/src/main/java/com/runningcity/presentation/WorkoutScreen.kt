package com.runningcity.presentation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.wear.compose.material.*
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.local.entity.HeartRateRecordEntity
import com.runningcity.data.local.entity.WorkoutSessionEntity
import com.runningcity.service.WorkoutService
import com.runningcity.utils.CsvExporter
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun WorkoutScreen(context: Context) {
    // 센서 데이터 상태
    var heartRate by remember { mutableStateOf(0) }
    var steps by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    // GPS 데이터 상태
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var gpsAccuracy by remember { mutableStateOf(0f) }
    var gpsCount by remember { mutableStateOf(0) }

    // ✅ 거리 상태 추가!
    var totalDistance by remember { mutableStateOf(0f) }

    // 운동 세션 상태
    var watchSessionId by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(0L) }

    // DB & CSV
    val database = remember { WorkoutDatabase.getDatabase(context) }
    val dao = database.workoutDao()
    val scope = rememberCoroutineScope()

    // 데이터 수집용 리스트
    val heartRateList = remember { mutableListOf<HeartRateRecordEntity>() }

    // 센서 매니저
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Service 연결
    var workoutService by remember { mutableStateOf<WorkoutService?>(null) }
    var serviceBound by remember { mutableStateOf(false) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WorkoutService.WorkoutBinder
                workoutService = binder.getService()
                serviceBound = true
                println("✅ Service 연결됨")

                // 위치 업데이트 리스너 설정
                workoutService?.onLocationUpdate = { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                    gpsAccuracy = location.accuracy
                    gpsCount++
                }
                // ✅ 거리 업데이트 리스너 추가!
                workoutService?.onDistanceUpdate = { distance ->
                    totalDistance = distance
                    println("📱 UI 거리 업데이트: ${distance}m")
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                workoutService = null
                serviceBound = false
                println("❌ Service 연결 해제됨")
            }
        }
    }

    // 심박수 센서
    val heartRateSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    }

    // 걸음수 센서
    val stepSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    var initialSteps by remember { mutableStateOf(0f) }

    // 심박수 리스너
    val heartRateListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                heartRate = event.values[0].toInt()
                println("💓 심박수 측정됨: $heartRate bpm")

                if (isRunning && watchSessionId.isNotEmpty()) {
                    val record = HeartRateRecordEntity(
                        watchSessionId = watchSessionId,
                        timestamp = System.currentTimeMillis(),
                        heartRate = heartRate,
                        accuracy = 3
                    )
                    heartRateList.add(record)

                    scope.launch {
                        dao.insertHeartRate(record)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                println("💓 센서 정확도 변경: $accuracy")
            }
        }
    }

    // 걸음수 리스너
    val stepListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (initialSteps == 0f) {
                    initialSteps = event.values[0]
                }
                steps = (event.values[0] - initialSteps).toInt()
                println("👟 걸음수: $steps")
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    // 🔥 센서 시작/중지 (LaunchedEffect 사용)
    LaunchedEffect(isRunning) {
        if (isRunning) {
            println("🚀 센서 시작!")

            // 새 세션 생성
            watchSessionId = "run_${UUID.randomUUID().toString().substring(0, 8)}"
            startTime = System.currentTimeMillis()

            // 세션 DB에 저장
            val session = WorkoutSessionEntity(
                watchSessionId = watchSessionId,
                startTime = startTime,
                status = "IN_PROGRESS"
            )
            dao.insertSession(session)
            println("💾 세션 DB 저장: $watchSessionId")

            // 리스트 초기화
            heartRateList.clear()
            gpsCount = 0

            // 심박수 센서 시작
            heartRateSensor?.let {
                sensorManager.registerListener(
                    heartRateListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                println("✅ 심박수 센서 등록")
            }

            // 걸음수 센서 시작
            stepSensor?.let {
                sensorManager.registerListener(
                    stepListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
                println("✅ 걸음수 센서 등록")
            }

            // Service 시작
            val serviceIntent = Intent(context, WorkoutService::class.java).apply {
                action = WorkoutService.ACTION_START
            }
            context.startForegroundService(serviceIntent)

            // Service 바인딩
            context.bindService(
                Intent(context, WorkoutService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )

            // Service에 세션 ID 전달
            kotlinx.coroutines.delay(500)
            workoutService?.watchSessionId = watchSessionId

        } else {
            // 🔥 운동 종료 (isRunning == false가 되면 즉시 실행)
            if (watchSessionId.isNotEmpty()) {
                println("⏹️ 운동 종료! 센서/Service 중지 중...")

                // 🔥 먼저 센서 중지!
                println("⏹️ 센서 중지")
                sensorManager.unregisterListener(heartRateListener)
                sensorManager.unregisterListener(stepListener)

                // ✅ Service에서 최종 거리 가져오기
                val finalDistance = workoutService?.getTotalDistance() ?: 0f
                println("📏 최종 거리: ${finalDistance}m")

                // 🔥 Service 중지!
                val serviceIntent = Intent(context, WorkoutService::class.java).apply {
                    action = WorkoutService.ACTION_STOP
                }
                context.startService(serviceIntent)

                if (serviceBound) {
                    try {
                        context.unbindService(serviceConnection)
                        serviceBound = false
                        println("✅ Service 언바인딩 완료")
                    } catch (e: Exception) {
                        println("⚠️ Service 언바인딩 실패: ${e.message}")
                    }
                }

                // 데이터 저장
                println("💾 데이터 저장 시작...")
                val endTime = System.currentTimeMillis()
                val duration = ((endTime - startTime) / 1000).toInt()

                // 심박수 통계 계산
                val avgHr = dao.getAvgHeartRate(watchSessionId) ?: 0
                val maxHr = dao.getMaxHeartRate(watchSessionId) ?: 0
                val minHr = dao.getMinHeartRate(watchSessionId) ?: 0

                // GPS 데이터 가져오기
                val locations = dao.getLocations(watchSessionId)
// ✅ 세션 업데 이트 (거리 포함!)
                val session = WorkoutSessionEntity(
                    watchSessionId = watchSessionId,
                    startTime = startTime,
                    endTime = endTime,
                    duration = duration,
                    status = "COMPLETED",
                    totalSteps = steps,
                    totalDistance = finalDistance.toDouble(),  // ✅ 여기!
                    totalCalories = 0,
                    avgHeartRate = avgHr,
                    maxHeartRate = maxHr,
                    minHeartRate = if (minHr == 0) 999 else minHr,
                    avgCadence = 0
                )

                dao.updateSession(session)
                println("💾 세션 업데이트 완료!")

                // CSV 저장
                CsvExporter.exportAll(
                    context = context,
                    session = session,
                    heartRates = heartRateList,
                    locations = locations
                )

                println("✅ 모든 데이터 저장 완료!")
                println("📊 요약:")
                println("   - 걸음수: $steps")
                println("   - 거리: ${finalDistance}m")  // ✅ 추가!
                println("   - 평균 심박수: $avgHr bpm")
                println("   - 최대 심박수: $maxHr bpm")
                println("   - 지속 시간: $duration 초")
                println("   - 심박수 기록: ${heartRateList.size}개")
                println("   - GPS 기록: ${locations.size}개")

                // 세션 ID 초기화
                watchSessionId = ""
            }
        }
    }

    // 🔥 화면 종료 시 정리 (별도 DisposableEffect)
    DisposableEffect(Unit) {
        onDispose {
            if (isRunning) {
                println("⏹️ [화면 종료] 센서/Service 강제 중지")
                sensorManager.unregisterListener(heartRateListener)
                sensorManager.unregisterListener(stepListener)

                val serviceIntent = Intent(context, WorkoutService::class.java).apply {
                    action = WorkoutService.ACTION_STOP
                }
                context.startService(serviceIntent)

                if (serviceBound) {
                    try {
                        context.unbindService(serviceConnection)
                    } catch (e: Exception) {
                        println("⚠️ [화면 종료] Service 언바인딩 실패: ${e.message}")
                    }
                }
            }
        }
    }

    // UI
    Scaffold(
        timeText = { TimeText() }
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
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

            // 심박수 표시
            Text(
                text = "💓 $heartRate",
                style = MaterialTheme.typography.display2,
                color = if (heartRate > 0) Color.Red else Color.Gray
            )
            Text(
                text = "bpm",
                style = MaterialTheme.typography.caption1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 걸음수 표시
            Text(
                text = "👟 $steps",
                style = MaterialTheme.typography.display2,
                color = if (steps > 0) Color.Green else Color.Gray
            )
            Text(
                text = "steps",
                style = MaterialTheme.typography.caption1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // GPS 표시
            Text(
                text = "📍 GPS (${gpsCount}개)",
                style = MaterialTheme.typography.caption1
            )
            Text(
                text = String.format("%.6f", latitude),
                style = MaterialTheme.typography.caption2,
                color = if (latitude != 0.0) Color.Cyan else Color.Gray
            )
            Text(
                text = String.format("%.6f", longitude),
                style = MaterialTheme.typography.caption2,
                color = if (longitude != 0.0) Color.Cyan else Color.Gray
            )
            if (gpsAccuracy > 0) {
                Text(
                    text = "정확도: ${gpsAccuracy.toInt()}m",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Yellow
                )
            }

            // Service 상태
            if (serviceBound) {
                Text(
                    text = "🔔 백그라운드 추적 중",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Green
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 시작/중지 버튼
            Button(
                onClick = {
                    isRunning = !isRunning
                    println("🔘 버튼 클릭: isRunning = $isRunning")
                },
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = if (isRunning) {
                    ButtonDefaults.secondaryButtonColors()
                } else {
                    ButtonDefaults.primaryButtonColors()
                }
            ) {
                Text(if (isRunning) "중지" else "시작")
            }

            if (isRunning) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "측정 중...",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Yellow
                )
                Text(
                    text = "ID: $watchSessionId",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}