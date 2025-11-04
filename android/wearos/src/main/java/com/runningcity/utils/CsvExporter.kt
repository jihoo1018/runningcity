package com.runningcity.utils

import android.content.Context
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

        // 데이터
        csv.append("세션ID,${session.watchSessionId}\n")
//        csv.append("운동타입,${session.exerciseType}\n")
        csv.append("시작시간,${timestampFormat.format(Date(session.startTime))}\n")
        csv.append("종료시간,${session.endTime?.let { timestampFormat.format(Date(it)) } ?: "진행중"}\n")
        csv.append("지속시간(초),${session.duration ?: 0}\n")
        csv.append("상태,${session.status}\n")
        csv.append("총걸음수,${session.totalSteps}\n")
        csv.append("총거리(m),${session.totalDistance}\n")
        csv.append("총칼로리,${session.totalCalories}\n")
        csv.append("평균심박수,${session.avgHeartRate}\n")
        csv.append("최대심박수,${session.maxHeartRate}\n")
        csv.append("최소심박수,${session.minHeartRate}\n")
        csv.append("평균케이던스,${session.avgCadence}\n")

        file.writeText(csv.toString())
        println("💾 세션 CSV 저장: ${file.absolutePath}")

        return file
    }

    /**
     * 심박수 기록을 CSV로 저장
     */
    fun exportHeartRates(
        context: Context,
        watchSessionId: String,
        records: List<HeartRateRecordEntity>
    ): File {
        val filename = "heartrate_${watchSessionId}_${dateFormat.format(Date())}.csv"
        val file = File(context.filesDir, filename)

        val csv = StringBuilder()

        // 헤더
        csv.append("시간,심박수(bpm),정확도\n")

        // 데이터
        records.forEach { record ->
            csv.append("${timestampFormat.format(Date(record.timestamp))},")
            csv.append("${record.heartRate},")
            csv.append("${record.accuracy}\n")
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
        watchSessionId: String,
        records: List<LocationRecordEntity>
    ): File {
        val filename = "location_${watchSessionId}_${dateFormat.format(Date())}.csv"
        val file = File(context.filesDir, filename)

        val csv = StringBuilder()

        // 헤더
        csv.append("시간,위도,경도,정확도(m),고도(m),속도(m/s)\n")

        // 데이터
        records.forEach { record ->
            csv.append("${timestampFormat.format(Date(record.timestamp))},")
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
     * 전체 데이터를 한 번에 CSV로 저장
     */
    fun exportAll(
        context: Context,
        session: WorkoutSessionEntity,
        heartRates: List<HeartRateRecordEntity>,
        locations: List<LocationRecordEntity>
    ): List<File> {
        val files = mutableListOf<File>()

        files.add(exportSession(context, session))

        if (heartRates.isNotEmpty()) {
            files.add(exportHeartRates(context, session.watchSessionId, heartRates))
        }

        if (locations.isNotEmpty()) {
            files.add(exportLocations(context, session.watchSessionId, locations))
        }

        println("✅ 총 ${files.size}개 CSV 파일 저장 완료!")
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
}