package com.runningcity.utils

import android.content.Context
import com.runningcity.data.local.entity.CadenceRecordEntity
import com.runningcity.data.local.entity.CalorieRecordEntity
import com.runningcity.data.local.entity.HeartRateRecordEntity
import com.runningcity.data.local.entity.LocationRecordEntity
import com.runningcity.data.local.entity.WorkoutSessionEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * 운동 세션을 CSV로 저장
     */
    fun exportSession(
        context: Context,
        session: WorkoutSessionEntity
    ): File {
        val filename = "session_${dateFormat.format(Date(session.startTime))}.csv"
        val file = File(context.filesDir, filename)

        val csv = StringBuilder()

        // 헤더
        csv.append("항목,값\n")

        // 데이터 - ✅ Entity 필드명에 맞춤
        csv.append("세션Seq,${session.seq}\n")  // ✅ seq 추가
        csv.append("세션Key,${session.clientSecretKey}\n")  // ✅ clientSecretKey
        csv.append("사용자ID,${session.userId}\n")
        csv.append("시작시간,${timestampFormat.format(Date(session.startTime))}\n")
        csv.append("종료시간,${session.endTime?.let { timestampFormat.format(Date(it)) } ?: "진행중"}\n")
        csv.append("지속시간(초),${session.duration ?: 0}\n")
        csv.append("상태,${session.status}\n")
        csv.append("총걸음수,${session.totalSteps}\n")
        csv.append("총거리(m),${String.format("%.2f", session.totalDistance)}\n")
        csv.append("총칼로리(kcal),${session.totalCalories}\n")
        csv.append("평균심박수(bpm),${session.avgHeartRate}\n")
        csv.append("평균케이던스(spm),${session.avgCadence}\n")
        csv.append("평균페이스(초/km),${session.avgPace}\n")  // ✅ avgPace 추가
        csv.append("평균고도(m),${String.format("%.2f", session.elevation)}\n")  // ✅ elevation 추가
        csv.append("생성시각,${timestampFormat.format(Date(session.createdAt))}\n")  // ✅ createdAt
        csv.append("수정시각,${timestampFormat.format(Date(session.updatedAt))}\n")  // ✅ updatedAt

        file.writeText(csv.toString())
        println("💾 세션 CSV 저장: ${file.absolutePath}")

        return file
    }

    /**
     * 심박수 기록을 CSV로 저장
     */
    fun exportHeartRates(
        context: Context,
        clientSecretKey: String,  // ✅ 파라미터명 변경
        records: List<HeartRateRecordEntity>
    ): File {
        val filename = "heartrate_${clientSecretKey}_${dateFormat.format(Date())}.csv"
        val file = File(context.filesDir, filename)

        val csv = StringBuilder()

        // 헤더 - ✅ Entity 필드명에 맞춤
        csv.append("Seq,세션Seq,시간,심박수(bpm)\n")  // ✅ accuracy 제거, 필드 추가

        // 데이터
        records.forEach { record ->
            csv.append("${record.seq},")  // ✅ seq 추가
            csv.append("${record.workoutSessionSeq},")  // ✅ workoutSessionSeq 추가
            csv.append("${timestampFormat.format(Date(record.createdAt))},")  // ✅ createdAt
            csv.append("${record.heartRate}\n")
        }

        file.writeText(csv.toString())
        println("💓 심박수 CSV 저장: ${file.absolutePath} (${records.size}개)")

        return file
    }

    /**
     * GPS 위치 기록을 CSV로 저장
     */
    fun exportLocations(
        context: Context,
        clientSecretKey: String,  // ✅ 파라미터명 변경
        records: List<LocationRecordEntity>
    ): File {
        val filename = "location_${clientSecretKey}_${dateFormat.format(Date())}.csv"
        val file = File(context.filesDir, filename)

        val csv = StringBuilder()

        // 헤더 - ✅ Entity 필드명에 맞춤
        csv.append("Seq,세션Seq,시간,위도,경도,정확도(m),고도(m),속도(m/s)\n")  // ✅ 필드 추가

        // 데이터
        records.forEach { record ->
            csv.append("${record.seq},")  // ✅ seq 추가
            csv.append("${record.workoutSessionSeq},")  // ✅ workoutSessionSeq 추가
            csv.append("${timestampFormat.format(Date(record.createdAt))},")  // ✅ createdAt
            csv.append("${record.latitude},")
            csv.append("${record.longitude},")
            csv.append("${record.accuracy},")
            csv.append("${record.altitude ?: ""},")
            csv.append("${record.speed ?: ""}\n")
        }

        file.writeText(csv.toString())
        println("📍 GPS CSV 저장: ${file.absolutePath} (${records.size}개)")

        return file
    }

    /**
     * 케이던스 기록을 CSV로 저장
     */
    fun exportCadences(
        context: Context,
        clientSecretKey: String,  // ✅ 파라미터명 변경
        records: List<CadenceRecordEntity>
    ): File {
        val filename = "cadence_${clientSecretKey}_${dateFormat.format(Date())}.csv"
        val file = File(context.filesDir, filename)

        val csv = StringBuilder()

        // 헤더 - ✅ Entity 필드명에 맞춤
        csv.append("Seq,세션Seq,시간,케이던스(spm)\n")  // ✅ accuracy 제거, 필드 추가

        // 데이터
        records.forEach { record ->
            csv.append("${record.seq},")  // ✅ seq 추가
            csv.append("${record.workoutSessionSeq},")  // ✅ workoutSessionSeq 추가
            csv.append("${timestampFormat.format(Date(record.createdAt))},")  // ✅ createdAt
            csv.append("${record.cadence}\n")
        }

        file.writeText(csv.toString())
        println("🏃 케이던스 CSV 저장: ${file.absolutePath} (${records.size}개)")

        return file
    }

    /**
     * 칼로리 기록을 CSV로 저장
     */
    fun exportCalories(
        context: Context,
        clientSecretKey: String,  // ✅ 파라미터명 변경
        records: List<CalorieRecordEntity>
    ): File {
        val filename = "calorie_${clientSecretKey}_${dateFormat.format(Date())}.csv"
        val file = File(context.filesDir, filename)

        val csv = StringBuilder()

        // 헤더 - ✅ Entity 필드명에 맞춤
        csv.append("Seq,세션Seq,시간,누적칼로리(kcal),증가량(kcal)\n")  // ✅ 필드 추가

        // 데이터
        records.forEach { record ->
            csv.append("${record.seq},")  // ✅ seq 추가
            csv.append("${record.workoutSessionSeq},")  // ✅ workoutSessionSeq 추가
            csv.append("${timestampFormat.format(Date(record.createdAt))},")  // ✅ createdAt
            csv.append("${String.format("%.2f", record.calories)},")
            csv.append("${record.caloriesIncrement?.let { String.format("%.2f", it) } ?: ""}\n")
        }

        file.writeText(csv.toString())
        println("🔥 칼로리 CSV 저장: ${file.absolutePath} (${records.size}개)")

        return file
    }

    /**
     * 전체 데이터를 한 번에 CSV로 저장
     */
    fun exportAll(
        context: Context,
        session: WorkoutSessionEntity,
        heartRates: List<HeartRateRecordEntity>,
        locations: List<LocationRecordEntity>,
        cadences: List<CadenceRecordEntity> = emptyList(),
        calories: List<CalorieRecordEntity> = emptyList()
    ): List<File> {
        val files = mutableListOf<File>()

        // 세션 정보
        files.add(exportSession(context, session))

        // 심박수
        if (heartRates.isNotEmpty()) {
            files.add(exportHeartRates(context, session.clientSecretKey, heartRates))  // ✅ 변경
        }

        // GPS
        if (locations.isNotEmpty()) {
            files.add(exportLocations(context, session.clientSecretKey, locations))  // ✅ 변경
        }

        // 케이던스
        if (cadences.isNotEmpty()) {
            files.add(exportCadences(context, session.clientSecretKey, cadences))  // ✅ 변경
        }

        // 칼로리
        if (calories.isNotEmpty()) {
            files.add(exportCalories(context, session.clientSecretKey, calories))  // ✅ 변경
        }

        println("✅ 총 ${files.size}개 CSV 파일 저장 완료!")
        files.forEachIndexed { index, file ->
            println("   ${index + 1}. ${file.name}")
        }

        return files
    }

    /**
     * 저장된 모든 CSV 파일 목록 조회
     */
    fun getAllCsvFiles(context: Context): List<File> {
        return context.filesDir.listFiles { file ->
            file.extension == "csv"
        }?.toList() ?: emptyList()
    }

    /**
     * CSV 파일 내용 읽기 (확인용)
     */
    fun readCsvFile(file: File): String {
        return file.readText()
    }

    /**
     * 특정 세션의 모든 CSV 파일 가져오기
     */
    fun getSessionCsvFiles(context: Context, clientSecretKey: String): List<File> {  // ✅ 파라미터명 변경
        return context.filesDir.listFiles { file ->
            file.extension == "csv" && file.name.contains(clientSecretKey)
        }?.toList() ?: emptyList()
    }

    /**
     * CSV 파일 삭제
     */
    fun deleteCsvFile(file: File): Boolean {
        return try {
            val deleted = file.delete()
            if (deleted) {
                println("🗑️ CSV 삭제: ${file.name}")
            }
            deleted
        } catch (e: Exception) {
            println("❌ CSV 삭제 실패: ${e.message}")
            false
        }
    }

    /**
     * 오래된 CSV 파일 일괄 삭제
     */
    fun deleteOldCsvFiles(context: Context, olderThanDays: Int = 30): Int {
        val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
        var deletedCount = 0

        context.filesDir.listFiles { file ->
            file.extension == "csv" && file.lastModified() < cutoffTime
        }?.forEach { file ->
            if (file.delete()) {
                deletedCount++
                println("🗑️ 오래된 CSV 삭제: ${file.name}")
            }
        }

        println("✅ 총 ${deletedCount}개 CSV 파일 삭제")
        return deletedCount
    }
}