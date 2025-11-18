package com.runningcity.presentation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*
import com.runningcity.service.DataSyncService
import com.runningcity.utils.PermissionManager
import kotlinx.coroutines.launch

class RunningActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager
    
    // Activity 레벨에서 화면 상태 관리 (onNewIntent에서 접근 가능)
    private var currentScreen = mutableStateOf<AppScreen>(AppScreen.Home)
    private var resultSessionSeq = mutableStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 워치 화면 자동 켜기
        turnOnScreen()

        permissionManager = PermissionManager(this)
        startDataSyncService()

        setContent {
            MaterialTheme {
                RunningCityNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // 워치 화면 자동 켜기 (이미 실행 중인 경우에도)
        turnOnScreen()

        intent?.let {
            val autoStart = it.getBooleanExtra("autoStart", false)
            val sessionId = it.getLongExtra("sessionId", 0L)
            val action = it.getStringExtra("action")

            // 러닝 시작 요청
            if (autoStart) {
                println("🚀 onNewIntent: 자동 시작 요청 수신 (sessionId: $sessionId)")

                // 모바일에 러닝 시작 알림 (onStartWorkout의 주요 로직)
                kotlinx.coroutines.GlobalScope.launch {
                    com.runningcity.utils.MobileCommunicationHelper.notifyStartRunning(this@RunningActivity)
                }

                val broadcastIntent = android.content.Intent("com.runningcity.START_WORKOUT_FROM_MOBILE")
                broadcastIntent.putExtra("sessionId", sessionId)
                sendBroadcast(broadcastIntent)

                // 화면을 WorkoutScreen으로 전환
                if (permissionManager.hasAllPermissions()) {
                    currentScreen.value = AppScreen.Workout
                    println("✅ 화면 전환: HomeScreen → WorkoutScreen")
                } else {
                    println("⚠️ 권한이 없어서 화면 전환 불가")
                }
            }
            
            // 일시정지/재개/종료 요청
            when (action) {
                "PAUSE" -> {
                    println("⏸️ onNewIntent: 일시정지 요청 수신")
                    // WorkoutService에 직접 일시정지 명령 전송
                    val serviceIntent = android.content.Intent(this, com.runningcity.service.WorkoutService::class.java).apply {
                        this.action = "ACTION_PAUSE"
                    }
                    startService(serviceIntent)
                    // WorkoutScreen 상태 업데이트를 위한 브로드캐스트
                    val broadcastIntent = android.content.Intent("com.runningcity.PAUSE_WORKOUT_FROM_MOBILE")
                    sendBroadcast(broadcastIntent)
                }
                "RESUME" -> {
                    println("▶️ onNewIntent: 재개 요청 수신")
                    // WorkoutService에 직접 재개 명령 전송
                    val serviceIntent = android.content.Intent(this, com.runningcity.service.WorkoutService::class.java).apply {
                        this.action = "ACTION_RESUME"
                    }
                    startService(serviceIntent)
                    // WorkoutScreen 상태 업데이트를 위한 브로드캐스트
                    val broadcastIntent = android.content.Intent("com.runningcity.RESUME_WORKOUT_FROM_MOBILE")
                    sendBroadcast(broadcastIntent)
                }
                "STOP" -> {
                    println("⏹️ onNewIntent: 종료 요청 수신")
                    // WorkoutScreen 종료 처리를 위한 브로드캐스트
                    val broadcastIntent = android.content.Intent("com.runningcity.STOP_WORKOUT_FROM_MOBILE")
                    sendBroadcast(broadcastIntent)
                }
            }
        }
    }

    private fun startDataSyncService() {
        try {
            val intent = Intent(this, DataSyncService::class.java)
            ContextCompat.startForegroundService(this, intent)
        } catch (e: Exception) {
            println("⚠️ DataSyncService 시작 실패: ${e.message}")
        }
    }

    /**
     * 워치 화면 자동 켜기
     */
    private fun turnOnScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // API 27+
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            // API 26 이하
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        println("📱 워치 화면 켜기 완료")
    }

    @Composable
    fun RunningCityNavigation() {
        val permissionMgr = this@RunningActivity.permissionManager
        val context = this@RunningActivity
        val scope = rememberCoroutineScope()

        // Activity 레벨 state를 Composable에서 관찰
        val screen by this@RunningActivity.currentScreen
        val sessionSeq by this@RunningActivity.resultSessionSeq
        
        var autoStartSessionId by remember { mutableStateOf<Long?>(null) }
        var intentProcessed by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (!intentProcessed) {
                val autoStart = context.intent.getBooleanExtra("autoStart", false)
                val sessionId = context.intent.getLongExtra("sessionId", 0L)

                if (autoStart) {
                    println("🚀 LaunchedEffect: 자동 시작 요청 수신 (sessionId: $sessionId)")
                    autoStartSessionId = sessionId
                    intentProcessed = true

                    // 모바일에 러닝 시작 알림 (onStartWorkout의 주요 로직)
                    scope.launch {
                        com.runningcity.utils.MobileCommunicationHelper.notifyStartRunning(context)
                    }

                    val broadcastIntent = android.content.Intent("com.runningcity.START_WORKOUT_FROM_MOBILE")
                    broadcastIntent.putExtra("sessionId", sessionId)
                    context.sendBroadcast(broadcastIntent)

                    if (permissionMgr.hasAllPermissions()) {
                        this@RunningActivity.currentScreen.value = AppScreen.Workout
                        println("✅ 화면 전환: HomeScreen → WorkoutScreen (onCreate)")
                    }
                }
            }
        }

        when (screen) {
            is AppScreen.Home -> {
                HomeScreen(
                    onStartWorkout = {
                        // 워치에서 모바일로 러닝 시작 요청 전송
                        scope.launch {
                            com.runningcity.utils.MobileCommunicationHelper.notifyStartRunning(context)
                        }
                        
                        if (permissionMgr.hasAllPermissions()) {
                            this@RunningActivity.currentScreen.value = AppScreen.Workout
                        } else {
                            permissionMgr.requestPermissions { granted ->
                                if (granted) {
                                    this@RunningActivity.currentScreen.value = AppScreen.Workout
                                } else {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        }
                    }
                )
            }

            is AppScreen.Workout -> {
                WorkoutScreen(
                    context = context,
                    onWorkoutComplete = { seq ->
                        this@RunningActivity.resultSessionSeq.value = seq
                        this@RunningActivity.currentScreen.value = AppScreen.Result
                    }
                )
            }

            is AppScreen.Result -> {
                WorkoutResultScreen(
                    context = context,
                    sessionSeq = sessionSeq,
                    onBackToHome = {
                        this@RunningActivity.currentScreen.value = AppScreen.Home
                    }
                )
            }

            else -> {
                this@RunningActivity.currentScreen.value = AppScreen.Home
            }
        }
    }
}

sealed class AppScreen {
    object Home : AppScreen()
    object Workout : AppScreen()
    object Result : AppScreen()
}