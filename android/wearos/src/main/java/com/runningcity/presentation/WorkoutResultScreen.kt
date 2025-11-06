package com.runningcity.presentation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.launch

@Composable
fun WorkoutResultScreen(
    context: Context,
    sessionSeq: Long,
    onBackToHome: () -> Unit
) {
    val database = remember { WorkoutDatabase.getDatabase(context) }
    val dao = database.workoutDao()
    val scope = rememberCoroutineScope()

    var session by remember { mutableStateOf<WorkoutSessionEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 세션 데이터 로드
    LaunchedEffect(sessionSeq) {
        scope.launch {
            session = dao.getSessionBySeq(sessionSeq)
            isLoading = false
            println("📊 결과 화면 로드: $session")
        }
    }

    Scaffold(
        timeText = { TimeText() }
    ) {
        val scrollState = rememberScrollState()

        if (isLoading) {
            // 로딩 화면
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (session != null) {
            // 결과 화면
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .verticalScroll(scrollState)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 타이틀
                Text(
                    text = "🎉 운동 완료!",
                    style = MaterialTheme.typography.title2,
                    color = Color.Green
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 주요 지표
                ResultCard(
                    icon = "📏",
                    label = "거리",
                    value = "${String.format("%.2f", session!!.totalDistance / 1000.0)} km",
                    color = Color.Cyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                ResultCard(
                    icon = "⏱️",
                    label = "시간",
                    value = formatDuration(session!!.duration ?: 0),
                    color = Color.Yellow
                )

                Spacer(modifier = Modifier.height(8.dp))

                ResultCard(
                    icon = "🔥",
                    label = "칼로리",
                    value = "${session!!.totalCalories} kcal",
                    color = Color(0xFFFF9800)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ResultCard(
                    icon = "👟",
                    label = "걸음",
                    value = "${session!!.totalSteps} 걸음",
                    color = Color.Green
                )

                Spacer(modifier = Modifier.height(8.dp))

                ResultCard(
                    icon = "💓",
                    label = "평균 심박수",
                    value = "${session!!.avgHeartRate} bpm",
                    color = Color.Red
                )

                Spacer(modifier = Modifier.height(8.dp))

                ResultCard(
                    icon = "🏃",
                    label = "평균 케이던스",
                    value = "${session!!.avgCadence} spm",
                    color = Color(0xFF9C27B0)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ResultCard(
                    icon = "⚡",
                    label = "평균 페이스",
                    value = formatPace(session!!.avgPace),
                    color = Color(0xFF00BCD4)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ResultCard(
                    icon = "⛰️",
                    label = "평균 고도",
                    value = "${String.format("%.1f", session!!.elevation)} m",
                    color = Color(0xFF8BC34A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 홈으로 버튼
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Text("🏠 홈으로")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            // 데이터 없음
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "❌ 데이터 없음",
                        style = MaterialTheme.typography.title3,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onBackToHome) {
                        Text("홈으로")
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.body1
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.body2,
                color = Color.LightGray
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            color = color,
            textAlign = TextAlign.End
        )
    }
}

// 시간 포맷팅 (초 → "분:초")
fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}

// 페이스 포맷팅 (초/km → "분'초"/km")
fun formatPace(paceInSeconds: Int): String {
    if (paceInSeconds <= 0) return "0'00\"/km"

    val minutes = paceInSeconds / 60
    val seconds = paceInSeconds % 60
    return String.format("%d'%02d\"/km", minutes, seconds)
}