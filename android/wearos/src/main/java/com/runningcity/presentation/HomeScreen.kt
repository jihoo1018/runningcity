package com.runningcity.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*

@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit
) {
    Scaffold(
        timeText = { TimeText() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏃 러닝시티",
                    style = MaterialTheme.typography.title1,
                    color = Color.Green
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartWorkout,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.primaryButtonColors()
                ) {
                    Text("▶️ 운동 시작")
                }

                Spacer(modifier = Modifier.height(8.dp))

//                // 추가 메뉴 (선택사항)
//                Button(
//                    modifier = Modifier.fillMaxWidth(0.8f),
//                    colors = ButtonDefaults.secondaryButtonColors()
//                ) {
//                    Text("📊 기록 보기")
//                }
            }
        }
    }
}