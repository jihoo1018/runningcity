package com.runningcity.data.location

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.runningcity.data.location.LocationBufferManager
import com.runningcity.data.network.LocationApiService
import com.runningcity.domain.repository.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 📦 LocationRepositoryImpl
 * ────────────────────────────────────────────────
 * - Repository 인터페이스의 실제 구현체
 * - 러닝 세션 시작/중지 시 ForegroundService 제어
 * - 세션 종료 시 서버 업로드까지 처리
 * ────────────────────────────────────────────────
 */
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: LocationApiService
) : LocationRepository {

    /** 🟢 세션 시작 */
    override suspend fun startSession() = withContext(Dispatchers.IO) {
        // 1️⃣ 기존 버퍼 초기화
        LocationBufferManager.clear()

        // 2️⃣ ForegroundService 실행
        val intent = Intent(context, LocationService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    /** 🔴 세션 종료 */
    override suspend fun stopSession() = withContext(Dispatchers.IO) {
        // 1️⃣ ForegroundService 종료
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)

        // 2️⃣ 버퍼에 쌓인 위치 데이터 서버 업로드
        val batch = LocationBufferManager.flush()

        if (batch.isNotEmpty()) {
            try {
                val response = api.sendLocationBatch(batch)
                if (response.isSuccessful) {
                    println("✅ 서버 업로드 완료 (${batch.size}건)")
                } else {
                    println("⚠️ 업로드 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                println("🚨 업로드 중 예외 발생: ${e.message}")
            }
        } else {
            println("ℹ️ 업로드할 위치 데이터 없음.")
        }
    }
}
