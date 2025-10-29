package com.runningcity

import android.app.Application
import android.content.Context

/**
 * App
 * - 전역 context를 사용하기 위한 Application 클래스(일반 Activity의 context는 화면이 꺼지면 사라지기 때문에 전역용이 필요함)
 * - 패키지 선언 및 Android Application 관련 클래스 import
 * - AndroidManifest.xml에서 android:name=".App" 으로 등록해야 함
 */
class App : Application() { //앱 전체 생명주기에서 가장 먼저 생성되는 객체
    override fun onCreate() { // 앱이 시작될 때 한 번 실행.
        super.onCreate()
        context = applicationContext // 전역 변수 context에 applicationContext(앱 전체 Context)를 저장(이렇게 해두면 다른 클래스에서도 App.context로 Context를 쓸 수 있음)
    }

    companion object {//Java의 static처럼 동작.
        lateinit var context: Context // 즉, App.context로 어느 클래스에서도 Context 접근 가능.
            private set // private set은 외부에서 수정은 못 하게 막음.
    }
}
