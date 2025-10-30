package com.runningcity.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * LocationUpdateReceiver
 * - "LOCATION_UPDATE" 액션을 수신하는 BroadcastReceiver
 * - 서비스(LocationService)나 다른 컴포넌트에서 위치 변경 브로드캐스트를 보낼 때 작동
 */
class LocationUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Intent에서 위치 정보 추출
        val latitude = intent?.getDoubleExtra("latitude", 0.0)
        val longitude = intent?.getDoubleExtra("longitude", 0.0)

        // 간단히 토스트 메시지로 출력
        if (latitude != null && longitude != null) {
            val message = "현재 위치: 위도 $latitude, 경도 $longitude"
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

            // TODO: 이곳에 서버 전송이나 DB 저장 로직을 추가 가능
        }
    }
}
