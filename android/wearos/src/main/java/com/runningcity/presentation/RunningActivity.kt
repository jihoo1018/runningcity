package com.runningcity.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        intent?.let {
            val autoStart = it.getBooleanExtra("autoStart", false)
            val sessionId = it.getLongExtra("sessionId", 0L)

            if (autoStart && sessionId > 0) {
                println("🚀 onNewIntent: 자동 시작 요청 수신 (sessionId: $sessionId)")

                val broadcastIntent = android.content.Intent("com.runningcity.START_WORKOUT_FROM_MOBILE")
                broadcastIntent.putExtra("sessionId", sessionId)
                sendBroadcast(broadcastIntent)
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

    @Composable
    fun RunningCityNavigation() {
        val permissionMgr = this@RunningActivity.permissionManager
        val context = this@RunningActivity
        val scope = rememberCoroutineScope()

        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
        var resultSessionSeq by remember { mutableStateOf(0L) }
        var autoStartSessionId by remember { mutableStateOf<Long?>(null) }
        var intentProcessed by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (!intentProcessed) {
                val autoStart = context.intent.getBooleanExtra("autoStart", false)
                val sessionId = context.intent.getLongExtra("sessionId", 0L)

                if (autoStart && sessionId > 0) {
                    println("🚀 자동 시작 요청 수신 (sessionId: $sessionId)")
                    autoStartSessionId = sessionId
                    intentProcessed = true

                    val broadcastIntent = android.content.Intent("com.runningcity.START_WORKOUT_FROM_MOBILE")
                    broadcastIntent.putExtra("sessionId", sessionId)
                    context.sendBroadcast(broadcastIntent)

                    if (permissionMgr.hasAllPermissions()) {
                        currentScreen = AppScreen.Workout
                    }
                }
            }
        }

        when (currentScreen) {
            is AppScreen.Home -> {
                HomeScreen(
                    onStartWorkout = {
                        // 워치에서 모바일로 러닝 시작 요청 전송
                        scope.launch {
                            com.runningcity.utils.MobileCommunicationHelper.notifyStartRunning(context)
                        }
                        
                        if (permissionMgr.hasAllPermissions()) {
                            currentScreen = AppScreen.Workout
                        } else {
                            permissionMgr.requestPermissions { granted ->
                                if (granted) {
                                    currentScreen = AppScreen.Workout
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
                        resultSessionSeq = seq
                        currentScreen = AppScreen.Result
                    }
                )
            }

            is AppScreen.Result -> {
                WorkoutResultScreen(
                    context = context,
                    sessionSeq = resultSessionSeq,
                    onBackToHome = {
                        currentScreen = AppScreen.Home
                    }
                )
            }

            else -> {
                currentScreen = AppScreen.Home
            }
        }
    }
}

sealed class AppScreen {
    object Home : AppScreen()
    object Workout : AppScreen()
    object Result : AppScreen()
}