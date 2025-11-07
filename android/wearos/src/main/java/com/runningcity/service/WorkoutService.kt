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
import java.time.Duration

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

                // 1.0.0 버전 ExerciseConfig 생성 - 생성자 직접 사용
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

                // 콜백 저장
                exerciseCallback = callback

                // 1.0.0 버전 - setUpdateCallback 사용
                exerciseClient.setUpdateCallback(callback)

                // 운동 시작 - 1.0.0 버전에서는 startExerciseAsync 사용
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

    /** 📊 실시간 데이터 처리 */
    private fun processExerciseUpdate(update: ExerciseUpdate) {
        serviceScope.launch {
            try {
                val latestMetrics = update.latestMetrics
                val timestamp = System.currentTimeMillis()

                // 1️⃣ 심박수 처리
                try {
                    val heartRateData = latestMetrics.getData(DataType.HEART_RATE_BPM)
                    val heartRateList = heartRateData.toList()
                    if (heartRateList.isNotEmpty()) {
                        currentHeartRate = heartRateList.last().value.toInt()

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

                // 2️⃣ 위치 처리
                try {
                    val locationData = latestMetrics.getData(DataType.LOCATION)
                    val locationList = locationData.toList()
                    if (locationList.isNotEmpty()) {
                        val locationValue = locationList.last().value

                        val location = Location("HealthServices").apply {
                            latitude = locationValue.latitude
                            longitude = locationValue.longitude

                            // 1.0.0에서 altitude는 nullable
                            altitude = locationValue.altitude ?: 0.0

                            // bearing을 accuracy로 임시 사용 (정확도 정보가 없으면)
                            accuracy = locationValue.bearing?.toFloat() ?: 10f

                            time = System.currentTimeMillis()
                        }

                        // 속도는 별도 DataType에서 가져오기
                        val speedData = latestMetrics.getData(DataType.SPEED)
                        val speedList = speedData.toList()
                        if (speedList.isNotEmpty()) {
                            location.speed = speedList.last().value.toFloat()
                        }

                        withContext(Dispatchers.Main) {
                            handleLocation(location)
                        }
                    }
                } catch (e: Exception) {
                    println("❌ 위치 처리 실패: ${e.message}")
                }

                // 3️⃣ 누적 걸음수
                try {
                    latestMetrics.getData(DataType.STEPS_TOTAL)?.let { dataPoint ->
                        val newSteps = dataPoint.total.toLong()  // ✅ total 사용!
                        if (newSteps != sessionSteps) {
                            sessionSteps = newSteps
                            withContext(Dispatchers.Main) {
                                onStepsUpdate?.invoke(sessionSteps.toInt())
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("❌ 걸음수 처리 실패: ${e.message}")
                }

                // 4️⃣ 총 거리 업데이트
                try {
                    latestMetrics.getData(DataType.DISTANCE_TOTAL)?.let { dataPoint ->
                        val healthDistance = dataPoint.total.toFloat()  // ✅ total 사용!
                        if (healthDistance > totalDistance) {
                            totalDistance = healthDistance
                            withContext(Dispatchers.Main) {
                                onDistanceUpdate?.invoke(totalDistance)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("❌ 거리 처리 실패: ${e.message}")
                }


                // 5️⃣ 케이던스 계산
                if (timestamp - lastCadenceUpdate > 5000) {
                    calculateCadenceFromSteps()
                    lastCadenceUpdate = timestamp
                }

                // 알림 업데이트
                updateNotification("📡 Health Services | ${totalDistance.toInt()}m | ${"%.0f".format(totalCalories)}kcal | ${currentCadence}spm")

            } catch (e: Exception) {
                println("❌ Exercise Update 처리 실패: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /** 📍 위치 처리 */
    private fun handleLocation(location: Location) {
        if (isPaused) return

        // 정확도 필터링
        if (location.accuracy > 25f) return

        // 첫 위치는 기준점만 저장
        if (lastLocation == null) {
            lastLocation = location
            saveLocationToDb(location)
            return
        }

        // 거리 계산
        val distance = lastLocation!!.distanceTo(location)
        if (distance in 0f..50f) {
            totalDistance += distance
            onDistanceUpdate?.invoke(totalDistance)
        }

        // 고도 수집
        if (location.altitude != 0.0) {
            elevationList.add(location.altitude)
        }

        // 칼로리 계산
        calculateCalories(location)

        // 상태 업데이트
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
                // 1.0.0 버전에서는 pauseExerciseAsync().get() 사용
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
                // 1.0.0 버전에서는 resumeExerciseAsync().get() 사용
                exerciseClient.resumeExerciseAsync().get()
                isPaused = false

                // 위치 리셋
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

                // ✅ 콜백 인스턴스 직접 전달 (Class 아님!)
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
    }

    // ✅ 기존 Getter들 (100% 호환)
    fun getTotalDistance(): Float = totalDistance
    fun getTotalPausedDuration(): Long = totalPausedDuration
    fun isPaused(): Boolean = isPaused
    fun getTotalCalories(): Double = totalCalories
    fun getTotalSteps(): Int = sessionSteps.toInt()
    fun getCurrentCadence(): Int = currentCadence
    fun getElevationList(): List<Double> = elevationList.toList()
}