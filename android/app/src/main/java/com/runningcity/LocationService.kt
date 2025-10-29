package com.runningcity

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service // 백그라운드 실행용 기본 클래스
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat//포그라운드 알림 생성
import com.google.android.gms.location.FusedLocationProviderClient //구글의 GPS API
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope//코루틴 비동기 실행
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.cancel

/**
 * 백그라운드 GPS 추적 서비스
 * -
 */
class LocationService : Service() {//안드로이드 서비스 클래스. UI는 없지만, 앱이 꺼져도 계속 동작 가능.

    private lateinit var fusedClient: FusedLocationProviderClient//구글의 GPS API 객체 → 실제 위치를 받아오는 핵심 클래스.
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob()) //코루틴 스코프 생성.
    // Dispatchers.IO: 네트워크/파일작업 전용 스레드.
    // SupervisorJob(): 자식 코루틴이 실패해도 부모 스코프 유지.
    private val networkClient = NetworkClient()//서버 통신 담당 객체(NetworkClient.kt에서 정의됨).

    override fun onCreate() {//서비스 시작 시 1회 호출.
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)//GPS 기능 초기화
        startForeground(1, createNotification())//포그라운드 서비스로 실행 (알림 띄움).
        startTracking()//실제 위치 추적 시작.
    }

    //알림(Notification) 생성→ Android 8.0 이상에서는 “채널(channel)”로 알림을 관리해야 함.
    private fun createNotification(): Notification {
        val channelId = "runningcity_location"
        val manager = getSystemService(NotificationManager::class.java)

        //Android 8.0+용 알림 채널 생성.
        //IMPORTANCE_LOW: 조용히 표시되는 알림 (GPS 아이콘만 보이게).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "RunningCity GPS Service",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        //실제 포그라운드 알림 빌드.
        //setOngoing(true) → 사용자가 직접 종료할 수 없는 지속형 알림.
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("RunningCity GPS 전송 중")
            .setContentText("위치 데이터를 서버로 전송하고 있습니다.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    //위치 추적 시작
    private fun startTracking() {
        val request = LocationRequest.Builder(//LocationRequest: GPS 요청 설정.
            Priority.PRIORITY_HIGH_ACCURACY,//PRIORITY_HIGH_ACCURACY: 고정밀 GPS 사용
            TimeUnit.SECONDS.toMillis(3)//3초마다 위치 갱신 요청
        ).build()

        //위치 권한이 없으면 그냥 종료 (보안상 필수 체크).
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedClient.requestLocationUpdates(request, locationCallback, null)//실제로 GPS 업데이트를 요청. locationCallback이 호출될 때마다 새로운 위치를 전달받음.
    }

    //위치 콜백 정의
    private val locationCallback = object : LocationCallback() {//GPS 업데이트가 발생할 때마다 호출됨.
        override fun onLocationResult(result: LocationResult) {
            val location: Location? = result.lastLocation //result.lastLocation = 가장 최근 위치.
            location?.let {
                // UI로 브로드캐스트 전송
                val intent = Intent("LOCATION_UPDATE").apply {
                    putExtra("latitude", it.latitude)
                    putExtra("longitude", it.longitude)
                }
                sendBroadcast(intent) //위치 데이터를 Intent로 브로드캐스트해서 MainActivity의 UI에 실시간 전달함.

                // 서버 전송
                serviceScope.launch {
                    networkClient.sendLocation(it)
                }//코루틴을 사용해 서버에 비동기로 전송 (UI 멈추지 않게).sendLocation(it)은 NetworkClient.kt에서 처리.
            }
        }
    }

    //서비스 종료 시 정리
    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    } //GPS 업데이트 중단 및 코루틴 종료. 앱이 종료될 때 리소스 낭비 방지.

    override fun onBind(intent: Intent?): IBinder? {
        return null//“바운드 서비스”가 아니므로 null 반환.(다른 앱에서 연결하지 않음)
    }
}
