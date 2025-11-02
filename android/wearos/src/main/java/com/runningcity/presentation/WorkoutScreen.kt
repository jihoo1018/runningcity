package com.runningcity.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
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
import com.google.android.gms.location.*

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

    // 센서 매니저
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // GPS 클라이언트
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
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

    // 센서 하드웨어 체크
    LaunchedEffect(Unit) {
        if (heartRateSensor == null) {
            println("❌ 심박수 센서가 없습니다!")
        } else {
            println("✅ 심박수 센서 발견: ${heartRateSensor.name}")
        }

        if (stepSensor == null) {
            println("❌ 걸음수 센서가 없습니다!")
        } else {
            println("✅ 걸음수 센서 발견: ${stepSensor.name}")
        }

        println("✅ GPS 초기화 완료")
    }

    // 심박수 리스너
    val heartRateListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                heartRate = event.values[0].toInt()
                println("💓 심박수 측정됨: $heartRate bpm")
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

    // GPS 위치 콜백
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                    gpsAccuracy = location.accuracy

                    println("📍 GPS: 위도=${String.format("%.6f", latitude)}, 경도=${String.format("%.6f", longitude)}, 정확도=${gpsAccuracy}m")
                }
            }
        }
    }

    // 센서 및 GPS 시작/중지
    DisposableEffect(isRunning) {
        if (isRunning) {
            println("🚀 센서 시작!")

            // 심박수 센서 시작
            heartRateSensor?.let {
                sensorManager.registerListener(
                    heartRateListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }

            // 걸음수 센서 시작
            stepSensor?.let {
                sensorManager.registerListener(
                    stepListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }

            // GPS 시작
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    5000  // 5초마다 업데이트
                ).apply {
                    setMinUpdateIntervalMillis(2000)  // 최소 2초
                    setMaxUpdateDelayMillis(10000)
                }.build()

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                println("🚀 GPS 추적 시작!")
            } else {
                println("❌ GPS 권한 없음!")
            }
        }

        onDispose {
            if (isRunning) {
                println("⏹️ 센서 중지")
                sensorManager.unregisterListener(heartRateListener)
                sensorManager.unregisterListener(stepListener)
                fusedLocationClient.removeLocationUpdates(locationCallback)
                println("⏹️ GPS 추적 중지")
            }
        }
    }

    // UI - 스크롤 가능하게!
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
                text = "📍 GPS",
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

            // 상태 표시
            if (isRunning) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "측정 중...",
                    style = MaterialTheme.typography.caption2,
                    color = Color.Yellow
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}