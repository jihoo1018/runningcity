package com.runningcity.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.webkit.WebView
import com.runningcity.BuildConfig
import dagger.hilt.android.HiltAndroidApp

/**
 * 🌐 App.kt
 * ────────────────────────────────────────────────
 * - 전역 Context 관리용 Application 클래스
 * - ForegroundService용 NotificationChannel 등록
 * ────────────────────────────────────────────────
 */
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // 추가 영역
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }


        // ForegroundService NotificationChannel 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "running_location", // 채널 ID
                "러닝 위치 추적",     // 채널 이름
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
