package com.runningcity

import android.location.Location
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class NetworkClient {
    private val client = OkHttpClient()

    // 🚀 서버 주소 (ngrok 매번 새로 켤 때마다 갱신!)
    private val serverUrl = "https://nonconjunctive-cami-outdoor.ngrok-free.dev/api/location"

    suspend fun sendLocation(location: Location) {
        try {
            val json = JSONObject().apply {
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("accuracy", location.accuracy)        // 정확도 (m)
                put("altitude", location.altitude)        // 고도 (m)
                put("speed", location.speed)              // 속도 (m/s)
                put("bearing", location.bearing)          // 진행 방향 (도)
                put("provider", location.provider)        // 위치 제공자 (gps / network)
                put("timestamp", location.time)           // 측정 시각
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(serverUrl)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                Log.d("NetworkClient", "✅ Sent: ${json} | Code: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("NetworkClient", "❌ Error sending location", e)
        }
    }
}
