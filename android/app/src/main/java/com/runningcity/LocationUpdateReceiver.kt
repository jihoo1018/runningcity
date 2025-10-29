package com.runningcity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * LocationUpdateReceiver
 * - "LOCATION_UPDATE" 액션을 수신하여 위치 데이터를 처리하는 BroadcastReceiver
 */
class LocationUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // 브로드캐스트에서 위치 데이터 받기
        val latitude = intent?.getDoubleExtra("latitude", 0.0)
        val longitude = intent?.getDoubleExtra("longitude", 0.0)

        // 위치 데이터를 받아 처리 (예시: 토스트로 위치 표시)
        if (latitude != null && longitude != null) {
            val locationMessage = "현재 위치: 위도: $latitude, 경도: $longitude"
            Toast.makeText(context, locationMessage, Toast.LENGTH_LONG).show()

            // 서버로 위치 데이터 보내기 등 다른 작업을 추가할 수 있음
        }
    }
}
