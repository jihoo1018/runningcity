package com.runningcity

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class NetworkClient {
    private val client = OkHttpClient()

    // 🔧 여기에 당신의 서버 주소 입력
    private val serverUrl = "https://your-server.com/api/location"

    suspend fun sendLocation(lat: Double, lon: Double) {
        try {
            val json = JSONObject().apply {
                put("latitude", lat)
                put("longitude", lon)
                put("timestamp", System.currentTimeMillis())
            }

            val body = json.toString().toRequestBody("applica.tion/json".toMediaType())

            val request = Request.Builder()
                .url(serverUrl)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                Log.d("NetworkClient", "Sent: $lat,$lon | Code: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("NetworkClient", "Error sending location", e)
        }
    }
}
