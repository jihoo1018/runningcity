package com.runningcity

import android.app.Application
import android.content.Context

/**
 * App
 * - 전역 context를 사용하기 위한 Application 클래스
 * - AndroidManifest.xml에서 android:name=".App" 으로 등록해야 함
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        lateinit var context: Context
            private set
    }
}
