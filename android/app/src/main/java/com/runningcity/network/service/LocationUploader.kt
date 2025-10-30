package com.runningcity.network.service

import android.location.Location
import android.util.Log
import com.runningcity.network.NetworkModule
import com.runningcity.network.api.LocationApiService
import com.runningcity.network.model.LocationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * LocationUploader
 * - GPS 데이터를 받아 서버로 전송하는 역할
 * - Retrofit + Coroutine 기반 (백그라운드 I/O 스레드에서 실행)
 */
class LocationUploader {

    private val api = NetworkModule.retrofit.create(LocationApiService::class.java)

    /**
     * 서버로 위치 데이터를 전송하는 suspend 함수
     */
    suspend fun sendLocation(location: Location) = withContext(Dispatchers.IO) {
        try {
            val request = LocationRequest(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                altitude = location.altitude,
                speed = location.speed,
                bearing = location.bearing,
                provider = location.provider,
                timestamp = location.time
            )

            val response = api.sendLocation(request)
            if (response.isSuccessful) {
                Log.d("LocationUploader", "✅ 위치 전송 성공: $request | Code: ${response.code()}")
            } else {
                Log.e("LocationUploader", "❌ 서버 응답 오류: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e("LocationUploader", "❌ 네트워크 오류: ${e.message}", e)
        }
    }
}
