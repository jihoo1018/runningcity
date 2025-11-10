package com.runningcity.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.local.entity.CadenceRecordEntity
import com.runningcity.data.local.entity.CalorieRecordEntity
import com.runningcity.data.local.entity.LocationRecordEntity
import kotlinx.coroutines.*

class WorkoutService_backup : Service(), SensorEventListener {

    private val binder = WorkoutBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // GPS 관련
    private var gpsListener: LocationListener? = null
    private var networkListener: LocationListener? = null
    private lateinit var locationManager: LocationManager
    private var lastProvider: String? = null

    // 센서 관련
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var lastStepCount = 0f
    private var sessionSteps = 0

    // 세션 및 상태
    var clientSecretKey: String = ""
    var workoutSessionSeq: Long = 0L
    var isTracking = false
    private var isPaused = false

    // 시간
    private var pausedTime = 0L
    private var totalPausedDuration = 0L

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
    private val cadenceWindow = mutableListOf<Pair<Long, Int>>()

    // 고도 리스트 (평균용)
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
        fun getService(): WorkoutService_backup = this@WorkoutService_backup
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundService()
                startLocationTracking()
                startSensorTracking()
            }
            ACTION_PAUSE -> pauseWorkout()
            ACTION_RESUME -> resumeWorkout()
            ACTION_STOP -> {
                stopSensorTracking()
                stopLocationTracking()
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

    /** 📍 위치 추적 시작 */
    private fun startLocationTracking() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        resetDistance()
        setupLocationListeners()

        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val netEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (gpsEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000L, 3f, gpsListener!!
                )
            }
            if (netEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 5000L, 10f, networkListener!!
                )
            }
        }
        isTracking = true
    }

    /** 👟 센서 시작 */
    private fun startSensorTracking() {
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || isPaused) return
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val currentSteps = event.values[0]
            if (lastStepCount == 0f) {
                lastStepCount = currentSteps
            } else {
                val diff = (currentSteps - lastStepCount).toInt()
                if (diff > 0) {
                    sessionSteps += diff
                    lastStepCount = currentSteps
                    calculateCadence()
                    onStepsUpdate?.invoke(sessionSteps)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /** ⏱️ 케이던스 계산 */
    private fun calculateCadence() {
        val now = System.currentTimeMillis()
        cadenceWindow.add(now to 1)
        cadenceWindow.removeAll { now - it.first > 60000 }

        val oldest = cadenceWindow.firstOrNull()?.first ?: return
        val seconds = (now - oldest) / 1000.0
        if (seconds > 0) {
            val steps = cadenceWindow.size
            currentCadence = ((steps / seconds) * 60).toInt()
            if (now - lastCadenceUpdate > 5000) {
                lastCadenceUpdate = now
                onCadenceUpdate?.invoke(currentCadence)
                saveCadenceToDb()
            }
        }
    }

    /** 📍 위치 리스너 설정 */
    private fun setupLocationListeners() {
        gpsListener = object : LocationListener {
            override fun onLocationChanged(location: Location) =
                handleLocation(location, "GPS")
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        networkListener = object : LocationListener {
            override fun onLocationChanged(location: Location) =
                handleLocation(location, "Network")
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

    /** 🧮 위치 계산 핵심 */
    private fun handleLocation(location: Location, source: String) {
        if (isPaused) return

        // ✅ 1. 정확도 & 속도 필터링
        if (location.accuracy > 25f || location.speed > 10f) return

        // ✅ 2. GPS 우선 (GPS 수신 중에는 Network 무시)
        if (source == "Network" && lastProvider == "GPS") return
        lastProvider = source

        // ✅ 3. 첫 위치는 기준점만 저장 (거리 계산 제외)
        if (lastLocation == null) {
            lastLocation = location
            return
        }

        // ✅ 4. 거리 계산 (튀는 값 필터링)
        val distance = lastLocation!!.distanceTo(location)
        if (distance in 0f..50f) { // 50m 이상 튀면 무시
            totalDistance += distance
            onDistanceUpdate?.invoke(totalDistance)
        }

        // ✅ 5. 고도 수집
        elevationList.add(location.altitude)

        // ✅ 6. 칼로리 계산
        calculateCalories(location)

        // ✅ 7. 상태 업데이트
        lastLocation = location
        updateNotification("📡 $source | ${totalDistance.toInt()}m | ${"%.0f".format(totalCalories)}kcal | ${currentCadence}spm")

        onLocationUpdate?.invoke(location)
        saveLocationToDb(location)
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

    /** 💾 DB 저장들 */
    private fun saveLocationToDb(location: Location) {
        if (workoutSessionSeq == 0L) return
        serviceScope.launch {
            try {
                val dao = WorkoutDatabase.getDatabase(applicationContext).workoutDao()
                dao.insertLocation(
                    LocationRecordEntity(
                        workoutSessionSeq = workoutSessionSeq,
                        createdAt = System.currentTimeMillis(),
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

    /** 📴 센서/위치 정리 */
    private fun stopSensorTracking() = sensorManager.unregisterListener(this)

    private fun stopLocationTracking() {
        gpsListener?.let { locationManager.removeUpdates(it) }
        networkListener?.let { locationManager.removeUpdates(it) }
        isTracking = false
    }

    /** ⏸️ 일시정지 */
    private fun pauseWorkout() {
        if (!isTracking || isPaused) return
        isTracking = false
        isPaused = true
        pausedTime = System.currentTimeMillis()
        updateNotification("⏸️ 일시정지 | ${totalDistance.toInt()}m | ${"%.0f".format(totalCalories)}kcal")
    }

    /** ▶️ 재개 */
    private fun resumeWorkout() {
        if (!isPaused) return
        isPaused = false
        isTracking = true

        // ✅ 위치 리셋 → 거리 튐 방지
        lastLocation = null

        if (pausedTime > 0) {
            totalPausedDuration += (System.currentTimeMillis() - pausedTime) / 1000
            pausedTime = 0L
        }
        updateNotification("▶️ 운동 중 | ${totalDistance.toInt()}m | ${"%.0f".format(totalCalories)}kcal")
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
        stopSensorTracking()
        stopLocationTracking()
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
        sessionSteps = 0
        lastStepCount = 0f
        currentCadence = 0
        lastCadenceUpdate = 0L
        cadenceWindow.clear()
        elevationList.clear()
    }

    // Getter들
    fun getTotalDistance(): Float = totalDistance
    fun getTotalPausedDuration(): Long = totalPausedDuration
    fun isPaused(): Boolean = isPaused
    fun getTotalCalories(): Double = totalCalories
    fun getTotalSteps(): Int = sessionSteps
    fun getCurrentCadence(): Int = currentCadence
    fun getElevationList(): List<Double> = elevationList.toList()
}
