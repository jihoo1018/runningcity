package com.runningcity.service

import android.app.*
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.clearUpdateCallback
import androidx.health.services.client.data.*
import androidx.health.services.client.endExercise
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.local.entity.CadenceRecordEntity
import com.runningcity.data.local.entity.CalorieRecordEntity
import com.runningcity.data.local.entity.HeartRateRecordEntity
import com.runningcity.data.local.entity.LocationRecordEntity
import kotlinx.coroutines.*

/**
 * Health Services 1.0.0 기반 운동 추적 서비스
 * 버전: androidx.health:health-services-client:1.0.0-rc02 (stable)
 */
class WorkoutService : Service() {

    private val binder = WorkoutBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Health Services 클라이언트
    private lateinit var exerciseClient: ExerciseClient
    private var isExerciseActive = false
    private var exerciseCallback: ExerciseUpdateCallback? = null

    // 세션 및 상태
    var clientSecretKey: String = ""
    var workoutSessionSeq: Long = 0L
    var isTracking = false
    private var isPaused = false

    // 시간
    private var pausedTime = 0L
    private var totalPausedDuration = 0L
    private var startTime = 0L

    // 거리 및 위치
    private var totalDistance = 0f
    private var lastLocation: Location? = null

    // 칼로리
    private var totalCalories = 0.0
    private var lastCalorieUpdate = 0L
    private val userWeight = 70.0  // kg

    // 케이던스
    private var currentCadence = 0
    private var lastCadenceUpdate = 0L

    // ⭐ 페이스 추가
    private var currentPace = 0  // 초/km 단위
    private var lastPaceUpdate = 0L

    // 걸음수
    private var sessionSteps = 0L

    // 심박수
    private var currentHeartRate = 0

    // 고도 리스트
    private val elevationList = mutableListOf<Double>()

    // 콜백 리스너
    var onLocationUpdate: ((Location) -> Unit)? = null
    var onDistanceUpdate: ((Float) -> Unit)? = null
    var onCalorieUpdate: ((Double) -> Unit)? = null
    var onCadenceUpdate: ((Int) -> Unit)? = null
    var onStepsUpdate: ((Int) -> Unit)? = null
    var onHeartRateUpdate: ((Int) -> Unit)? = null
    var onPaceUpdate: ((Int) -> Unit)? = null  // ⭐ 페이스 콜백 추가

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "workout_channel"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
    }

    inner class WorkoutBinder : Binder() {
        fun getService(): WorkoutService = this@WorkoutService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Health Services 초기화
        val healthServicesClient = HealthServices.getClient(this)
        exerciseClient = healthServicesClient.exerciseClient

        println("✅ Health Services 초기화 완료 (v1.0.0)")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundService()
                startHealthServicesTracking()
            }
            ACTION_PAUSE -> pauseWorkout()
            ACTION_RESUME -> resumeWorkout()
            ACTION_STOP -> {
                stopHealthServicesTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    /** 🔔 포그라운드 서비스 시작 */
    private fun startForegroundService() {
        val notification = createNotification("운동 추적 중...")
        startForeground(NOTIFICATION_ID, notification)
    }

    /** 🏃 Health Services 운동 시작 */
    private fun startHealthServicesTracking() {
        serviceScope.launch {
            try {
                startTime = System.currentTimeMillis()
                resetDistance()

                // 1.0.0 버전 ExerciseConfig 생성
                val config = ExerciseConfig(
                    exerciseType = ExerciseType.RUNNING,
                    dataTypes = setOf(
                        DataType.HEART_RATE_BPM,
                        DataType.LOCATION,
                        DataType.CALORIES_TOTAL,
                        DataType.STEPS_TOTAL,
                        DataType.SPEED,
                        DataType.DISTANCE_TOTAL
                    ),
                    isAutoPauseAndResumeEnabled = false,
                    isGpsEnabled = true
                )

                // 콜백 생성
                val callback = object : ExerciseUpdateCallback {
                    override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                        if (!isPaused) {
                            processExerciseUpdate(update)

                        }
                    }

                    override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
                        // 사용 안 함
                    }

                    override fun onRegistered() {
                        println("✅ Exercise Callback 등록 완료")
                    }

                    override fun onRegistrationFailed(throwable: Throwable) {
                        println("❌ Exercise Callback 등록 실패: ${throwable.message}")
                    }

                    override fun onAvailabilityChanged(
                        dataType: DataType<*, *>,
                        availability: Availability
                    ) {
                        println("📍 센서 상태 변경: $dataType = $availability")
                    }
                }

                exerciseCallback = callback
                exerciseClient.setUpdateCallback(callback)
                exerciseClient.startExerciseAsync(config).get()
                isTracking = true
                isExerciseActive = true

                println("✅ Health Services 운동 시작")

            } catch (e: Exception) {
                println("❌ Health Services 시작 실패: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /** 📊 실시간 데이터 처리 (디버깅 로그 포함 버전) */
    private fun processExerciseUpdate(update: ExerciseUpdate) {
        serviceScope.launch {
            try {
                val latestMetrics = update.latestMetrics
                val timestamp = System.currentTimeMillis()

                // 🧭 [1] 거리 / 속도 / 위치 데이터 전체 상태 로그
                try {
                    val distanceData = latestMetrics.getData(DataType.DISTANCE_TOTAL)
                    val speedData = latestMetrics.getData(DataType.SPEED)
                    val locationData = latestMetrics.getData(DataType.LOCATION)

                    val distanceValue = distanceData?.total ?: 0.0
                    val speedCount = speedData?.toList()?.size ?: 0
                    val locationCount = locationData?.toList()?.size ?: 0

                    println(
                        "📏 [DEBUG] DISTANCE_TOTAL=${"%.2f".format(distanceValue)}m, " +
                                "SPEED count=$speedCount, LOCATION count=$locationCount"
                    )
                } catch (e: Exception) {
                    println("❌ [DEBUG] 거리/속도/위치 데이터 접근 실패: ${e.message}")
                }
                // 🩸 [2] 심박수 처리
                try {
                    val heartRateData = latestMetrics.getData(DataType.HEART_RATE_BPM)
                    val heartRateList = heartRateData.toList()
                    if (heartRateList.isNotEmpty()) {
                        currentHeartRate = heartRateList.last().value.toInt()
                        println("💓 [DEBUG] HEART_RATE = $currentHeartRate bpm")

                        withContext(Dispatchers.Main) {
                            onHeartRateUpdate?.invoke(currentHeartRate)
                        }

                        if (workoutSessionSeq != 0L) {
                            saveHeartRateToDb(
                                heartRate = currentHeartRate,
                                timestamp = System.currentTimeMillis()
                            )
                        }
                    }
                } catch (e: Exception) {
                    println("❌ 심박수 처리 실패: ${e.message}")
                }

                // 📍 [3] 위치 처리
                try {
                    val locationData = latestMetrics.getData(DataType.LOCATION)
                    val locationList = locationData.toList()
                    if (locationList.isNotEmpty()) {
                        val locationValue = locationList.last().value
                        val location = Location("HealthServices").apply {
                            latitude = locationValue.latitude
                            longitude = locationValue.longitude
                            altitude = locationValue.altitude ?: 0.0
                            accuracy = locationValue.bearing?.toFloat() ?: 10f
                            time = System.currentTimeMillis()
                        }

                        // 속도 데이터
                        val speedData = latestMetrics.getData(DataType.SPEED)
                        val speedList = speedData.toList()
                        if (speedList.isNotEmpty()) {
                            location.speed = speedList.last().value.toFloat()
                        }

                        println(
                            "📍 [DEBUG] LOCATION(lat=${location.latitude}, lon=${location.longitude}, " +
                                    "alt=${"%.1f".format(location.altitude)}, speed=${"%.2f".format(location.speed)})"
                        )

                        withContext(Dispatchers.Main) {
                            handleLocation(location)
                        }
                    } else {
                        println("⚠️ [DEBUG] 위치 데이터 없음 (GPS 신호 미수신)")
                    }
                } catch (e: Exception) {
                    println("❌ 위치 처리 실패: ${e.message}")
                }

                // 🚶 [4] 걸음수 처리
                try {
                    latestMetrics.getData(DataType.STEPS_TOTAL)?.let { dataPoint ->
                        val newSteps = dataPoint.total.toLong()
                        if (newSteps != sessionSteps) {
                            sessionSteps = newSteps
                            println("👣 [DEBUG] STEPS_TOTAL = $sessionSteps")
                            withContext(Dispatchers.Main) {
                                onStepsUpdate?.invoke(sessionSteps.toInt())
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("❌ 걸음수 처리 실패: ${e.message}")
                }

                // 🛣️ [5] 거리 처리
                try {
                    latestMetrics.getData(DataType.DISTANCE_TOTAL)?.let { dataPoint ->
                        val healthDistance = dataPoint.total.toFloat()
                        if (healthDistance > totalDistance) {
                            totalDistance = healthDistance
                            println("📈 [DEBUG] DISTANCE_TOTAL 업데이트: ${"%.2f".format(totalDistance)}m")

                            withContext(Dispatchers.Main) {
                                onDistanceUpdate?.invoke(totalDistance)
                            }
                            updatePace()
                        }
                    }
                } catch (e: Exception) {
                    println("❌ 거리 처리 실패: ${e.message}")
                }

                // ⚙️ [6] 케이던스 계산 (5초 주기)
                if (timestamp - lastCadenceUpdate > 5000) {
                    calculateCadenceFromSteps()
                    lastCadenceUpdate = timestamp
                }

                // ⚙️ [7] 페이스 계산 (5초 주기)
                if (timestamp - lastPaceUpdate > 5000) {
                    updatePace()
                    lastPaceUpdate = timestamp
                }

                // 🔔 [8] 알림 업데이트 (디버그용)
                val paceMin = currentPace / 60
                val paceSec = currentPace % 60
                updateNotification(
                    "📡 ${totalDistance.toInt()}m | ${"%.0f".format(totalCalories)}kcal | " +
                            "${currentCadence}spm | ${paceMin}'${paceSec}\""
                )

            } catch (e: Exception) {
                println("❌ Exercise Update 처리 실패: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    /** 📍 위치 처리 */
    private fun handleLocation(location: Location) {
        if (isPaused) return

        if (location.accuracy > 25f) return

        if (lastLocation == null) {
            lastLocation = location
            saveLocationToDb(location)
            return
        }

        val distance = lastLocation!!.distanceTo(location)
        if (distance in 0f..50f) {
            totalDistance += distance
            onDistanceUpdate?.invoke(totalDistance)
            // ⭐ 거리 변경 시 페이스도 업데이트
            updatePace()
        }

        if (location.altitude != 0.0) {
            elevationList.add(location.altitude)
        }

        calculateCalories(location)

        lastLocation = location
        onLocationUpdate?.invoke(location)
        saveLocationToDb(location)
    }

    /** ⏱️ 케이던스 계산 */
    private fun calculateCadenceFromSteps() {
        val now = System.currentTimeMillis()
        val elapsedSeconds = (now - startTime - totalPausedDuration * 1000) / 1000.0

        if (elapsedSeconds > 0 && sessionSteps > 0) {
            currentCadence = ((sessionSteps / elapsedSeconds) * 60).toInt()
            onCadenceUpdate?.invoke(currentCadence)
            saveCadenceToDb()
        }
    }

    /** 🏃 페이스 계산 (초/km) - ⭐ 신규 추가 */
    private fun calculateCurrentPace(): Int {
        // 일시정지 시간을 제외한 실제 운동 시간 (초)
        val elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000.0) - totalPausedDuration

        // 거리가 0이거나 시간이 0이면 페이스를 계산할 수 없음
        if (totalDistance <= 0f || elapsedSeconds <= 0) {
            return 0
        }

        // 1km당 걸리는 시간 (초) = (총 운동시간 / 거리(m)) * 1000
        val pace = ((elapsedSeconds / totalDistance) * 1000).toInt()

        // 비정상적인 값 필터링 (너무 느리거나 빠른 경우)
        return when {
            pace < 180 -> 180  // 최소 3분/km (너무 빠름)
            pace > 1200 -> 1200  // 최대 20분/km (너무 느림)
            else -> pace
        }
    }

    /** 🏃 페이스 업데이트 - ⭐ 신규 추가 */
    private fun updatePace() {
        if (isPaused) return  // 일시정지 중에는 페이스 계산 안 함

        val newPace = calculateCurrentPace()

        // 페이스가 변경되었을 때만 업데이트 (너무 빈번한 업데이트 방지)
        if (newPace != currentPace && newPace > 0) {
            currentPace = newPace

            // UI 콜백 호출
            serviceScope.launch(Dispatchers.Main) {
                onPaceUpdate?.invoke(currentPace)
                println("🏃 페이스 업데이트: ${currentPace}초/km (${currentPace/60}'${currentPace%60}\")")
            }
        }
    }

    /** 🔥 칼로리 계산 */
    private fun calculateCalories(location: Location) {
        val now = System.currentTimeMillis()
        if (now - lastCalorieUpdate < 5000) return

        val met = calculateMET(location)
        val timeHr = 5.0 / 3600.0
        val cal = met * userWeight * timeHr
        totalCalories += cal
        lastCalorieUpdate = now

        onCalorieUpdate?.invoke(totalCalories)
        saveCalorieToDb(totalCalories, cal)
    }

    private fun calculateMET(location: Location): Double {
        val kmh = location.speed * 3.6
        return when {
            kmh < 4 -> 4.0
            kmh < 6 -> 6.0
            kmh < 8 -> 8.0
            kmh < 10 -> 10.0
            kmh < 12 -> 11.5
            else -> 13.0
        }
    }

    /** 💾 DB 저장 메서드들 */
    private fun saveHeartRateToDb(heartRate: Int, timestamp: Long) {
        if (workoutSessionSeq == 0L) return
        serviceScope.launch {
            try {
                val dao = WorkoutDatabase.getDatabase(applicationContext).workoutDao()
                dao.insertHeartRate(
                    HeartRateRecordEntity(
                        workoutSessionSeq = workoutSessionSeq,
                        createdAt = timestamp,
                        heartRate = heartRate,
                        syncedToServer = false,
                        savedAt = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                println("❌ 심박수 저장 실패: ${e.message}")
            }
        }
    }

    private fun saveLocationToDb(location: Location) {
        if (workoutSessionSeq == 0L) return
        serviceScope.launch {
            try {
                val dao = WorkoutDatabase.getDatabase(applicationContext).workoutDao()
                dao.insertLocation(
                    LocationRecordEntity(
                        workoutSessionSeq = workoutSessionSeq,
                        createdAt = location.time,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        altitude = location.altitude,
                        speed = location.speed,
                        syncedToServer = false,
                        savedAt = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                println("❌ 위치 저장 실패: ${e.message}")
            }
        }
    }

    private fun saveCalorieToDb(total: Double, inc: Double) {
        if (workoutSessionSeq == 0L) return
        serviceScope.launch {
            val dao = WorkoutDatabase.getDatabase(applicationContext).workoutDao()
            dao.insertCalorie(
                CalorieRecordEntity(
                    workoutSessionSeq = workoutSessionSeq,
                    createdAt = System.currentTimeMillis(),
                    calories = total,
                    caloriesIncrement = inc,
                    syncedToServer = false,
                    savedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun saveCadenceToDb() {
        if (!isTracking || workoutSessionSeq == 0L) return
        serviceScope.launch {
            val dao = WorkoutDatabase.getDatabase(applicationContext).workoutDao()
            dao.insertCadence(
                CadenceRecordEntity(
                    workoutSessionSeq = workoutSessionSeq,
                    createdAt = System.currentTimeMillis(),
                    cadence = currentCadence,
                    syncedToServer = false,
                    savedAt = System.currentTimeMillis()
                )
            )
        }
    }

    /** ⏸️ 일시정지 */
    private fun pauseWorkout() {
        if (!isTracking || isPaused) return

        serviceScope.launch {
            try {
                exerciseClient.pauseExerciseAsync().get()
                isPaused = true
                pausedTime = System.currentTimeMillis()

                updateNotification("⏸️ 일시정지 | ${totalDistance.toInt()}m | ${"%.0f".format(totalCalories)}kcal")
                println("⏸️ 운동 일시정지")
            } catch (e: Exception) {
                println("❌ 일시정지 실패: ${e.message}")
            }
        }
    }

    /** ▶️ 재개 */
    private fun resumeWorkout() {
        if (!isPaused) return

        serviceScope.launch {
            try {
                exerciseClient.resumeExerciseAsync().get()
                isPaused = false
                lastLocation = null

                if (pausedTime > 0) {
                    totalPausedDuration += (System.currentTimeMillis() - pausedTime) / 1000
                    pausedTime = 0L
                }

                updateNotification("▶️ 운동 중 | ${totalDistance.toInt()}m | ${"%.0f".format(totalCalories)}kcal")
                println("▶️ 운동 재개")
            } catch (e: Exception) {
                println("❌ 재개 실패: ${e.message}")
            }
        }
    }

    /** 🛑 Health Services 정리 */
    private fun stopHealthServicesTracking() {
        if (!isExerciseActive) return

        serviceScope.launch {
            try {
                exerciseClient.endExercise()

                exerciseCallback?.let { callback ->
                    exerciseClient.clearUpdateCallback(callback)
                }

                isTracking = false
                isExerciseActive = false
                println("✅ Health Services 종료")
            } catch (e: Exception) {
                println("❌ Health Services 종료 실패: ${e.message}")
            }
        }
    }

    /** 🔔 알림 관련 */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "운동 추적", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("러닝시티")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, createNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopHealthServicesTracking()
        serviceScope.cancel()
    }

    /** 🔄 리셋 */
    fun resetDistance() {
        totalDistance = 0f
        lastLocation = null
        isPaused = false
        pausedTime = 0L
        totalPausedDuration = 0L
        totalCalories = 0.0
        lastCalorieUpdate = System.currentTimeMillis()
        sessionSteps = 0L
        currentCadence = 0
        lastCadenceUpdate = 0L
        elevationList.clear()
        currentHeartRate = 0
        currentPace = 0  // ⭐ 페이스 초기화 추가
        lastPaceUpdate = 0L  // ⭐ 추가
    }

    // ✅ Getter들
    fun getTotalDistance(): Float = totalDistance
    fun getTotalPausedDuration(): Long = totalPausedDuration
    fun isPaused(): Boolean = isPaused
    fun getTotalCalories(): Double = totalCalories
    fun getTotalSteps(): Int = sessionSteps.toInt()
    fun getCurrentCadence(): Int = currentCadence
    fun getElevationList(): List<Double> = elevationList.toList()
    fun getCurrentPace(): Int = currentPace  // ⭐ 페이스 Getter 추가
}