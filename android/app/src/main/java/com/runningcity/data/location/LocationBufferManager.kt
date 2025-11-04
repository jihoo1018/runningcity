package com.runningcity.data.location

import android.location.Location
import com.runningcity.data.model.LocationDto
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 📦 LocationBufferManager
 * ────────────────────────────────────────────────
 * - ForegroundService(LocationService) 에서 수집한 위치 데이터를 임시 저장
 * - RepositoryImpl.stopSession() 시 flush() 로 전체 데이터 반환 및 초기화
 * - Thread-safe 하게 관리 (CopyOnWriteArrayList 사용)
 * ────────────────────────────────────────────────
 */
object LocationBufferManager {

    // 위치 데이터 버퍼 (Thread-safe)
    private val buffer = CopyOnWriteArrayList<LocationDto>()

    /**
     * ➕ 위치 추가
     * - Location → LocationDto 변환 후 버퍼에 저장
     */
    fun addLocation(location: Location) {
        val dto = LocationDto(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = location.altitude,
            speed = location.speed,
            bearing = location.bearing,
            provider = location.provider,
            timestamp = System.currentTimeMillis()
        )
        buffer.add(dto)
    }

    /**
     * 🧹 버퍼 초기화
     */
    fun clear() {
        buffer.clear()
    }

    /**
     * 🚀 버퍼에 있는 데이터를 반환 후 비우기
     * - stopSession() 호출 시 사용
     */
    fun flush(): List<LocationDto> {
        val copy = buffer.toList() // 현재 버퍼 내용을 복사
        buffer.clear()             // 비우기
        return copy
    }

    /**
     * 🔍 현재 버퍼 크기 확인 (디버그용)
     */
    fun size(): Int = buffer.size
}
