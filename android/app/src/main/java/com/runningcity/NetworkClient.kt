package com.runningcity

import android.location.Location
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/*서버 전송 담당*/
class NetworkClient {
    private val client = OkHttpClient() //HTTP 요청용 OkHttpClient 초기화. 안드로이드에서 가장 많이 쓰이는 네트워크 라이브러리.

    // 서버 API 주소 (ngrok 주소는 매번 새로 켤 때 갱신 필요)
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
                put("timestamp", location.time)           // 위치 측정 시간(밀리초)
            }//GPS 데이터를 JSON 형식으로 만듦. apply {} 블록은 Kotlin DSL 문법으로 깔끔하게 key-value 입력 가능.

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(serverUrl)
                .post(body)
                .build()
            //HTTP POST 요청으로 JSON 전송 준비. toRequestBody("application/json"): JSON 형태로 서버에 보낼 준비

            client.newCall(request).execute().use { response ->//실제로 요청 실행 (execute())
                Log.d("NetworkClient", "✅ Sent: ${json} | Code: ${response.code}")//서버 응답 코드(200, 400, 등) 로그로 출력.
            }
        } catch (e: Exception) {
            Log.e("NetworkClient", "❌ Error sending location", e)//네트워크 에러 발생 시 Logcat에 에러 표시.
        }
    }
}
