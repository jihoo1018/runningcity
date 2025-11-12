package com.runningcity.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.*
import com.runningcity.presentation.theme.RunningcityTheme
import com.runningcity.presentation.theme.accentGreen
import com.runningcity.presentation.theme.accentRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 심박수 측정 중 화면
 * 워치에서 심박수 측정이 진행되는 동안 표시되는 Activity
 */
class HeartRateMeasurementActivity : ComponentActivity() {
    
    private var currentHeartRate by mutableStateOf<Int?>(null)
    private var isMeasuring by mutableStateOf(true)
    private var elapsedSeconds by mutableStateOf(0)
    private var hasError by mutableStateOf(false)
    private var counterJob: kotlinx.coroutines.Job? = null
    
    private fun startCounter() {
        counterJob?.cancel()
        elapsedSeconds = 0
        counterJob = lifecycleScope.launch {
            while (isMeasuring) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }
    
    private val measurementReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.runningcity.HEART_RATE_MEASUREMENT_UPDATE" -> {
                    val heartRate = intent.getIntExtra("heartRate", 0)
                    if (heartRate > 0) {
                        currentHeartRate = heartRate
                    }
                }
                "com.runningcity.HEART_RATE_MEASUREMENT_COMPLETE" -> {
                    val heartRate = intent.getIntExtra("heartRate", 0)
                    currentHeartRate = heartRate
                    isMeasuring = false
                    // 2초 후 자동 종료
                    lifecycleScope.launch {
                        delay(2000)
                        finish()
                    }
                }
                "com.runningcity.HEART_RATE_MEASUREMENT_ERROR" -> {
                    isMeasuring = false
                    hasError = true
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 브로드캐스트 리시버 등록
        val filter = IntentFilter().apply {
            addAction("com.runningcity.HEART_RATE_MEASUREMENT_UPDATE")
            addAction("com.runningcity.HEART_RATE_MEASUREMENT_COMPLETE")
            addAction("com.runningcity.HEART_RATE_MEASUREMENT_ERROR")
        }
        registerReceiver(measurementReceiver, filter)
        
        setContent {
            RunningcityTheme {
                HeartRateMeasurementScreen(
                    currentHeartRate = currentHeartRate,
                    isMeasuring = isMeasuring,
                    elapsedSeconds = elapsedSeconds,
                    hasError = hasError,
                    onRetry = {
                        // 다시 시도
                        hasError = false
                        isMeasuring = true
                        currentHeartRate = null
                        elapsedSeconds = 0
                        
                        // 경과 시간 카운터 재시작
                        this@HeartRateMeasurementActivity.startCounter()
                        
                        // 서비스에 다시 측정 요청
                        val intent = Intent("com.runningcity.MEASURE_HEART_RATE")
                        sendBroadcast(intent)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
        
        // 경과 시간 카운터 시작
        startCounter()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(measurementReceiver)
        } catch (e: Exception) {
            // 이미 해제된 경우 무시
        }
    }
}

@Composable
fun HeartRateMeasurementScreen(
    currentHeartRate: Int?,
    isMeasuring: Boolean,
    elapsedSeconds: Int,
    hasError: Boolean,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    // 펄스 애니메이션
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 심박수 아이콘 (펄스 애니메이션)
            if (hasError) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "측정 실패",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colors.error
                )
            } else if (isMeasuring) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "심박수 측정 중",
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        },
                    tint = MaterialTheme.colors.accentRed
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "측정 완료",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colors.accentGreen
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // 심박수 값 표시
            if (currentHeartRate != null) {
                Text(
                    text = "$currentHeartRate",
                    style = MaterialTheme.typography.display1.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colors.onBackground
                )
                
                Text(
                    text = "bpm",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "측정 중...",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onBackground
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // 상태 메시지
            Text(
                text = when {
                    hasError -> "측정에 실패했습니다"
                    isMeasuring -> "손목에 워치를\n착용해주세요"
                    else -> "측정 완료!"
                },
                style = MaterialTheme.typography.caption1,
                color = if (hasError) {
                    MaterialTheme.colors.error
                } else {
                    MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                }
            )
            
            Spacer(Modifier.height(12.dp))
            
            // 경과 시간 표시 (측정 중일 때만)
            if (isMeasuring && !hasError) {
                Text(
                    text = "${elapsedSeconds}초",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.primary
                )
            }
            
            // 에러 발생 시 버튼 표시
            if (hasError) {
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 다시 시도 버튼
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text(
                            text = "다시 시도",
                            style = MaterialTheme.typography.button
                        )
                    }
                    
                    // 취소 버튼
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.surface
                        )
                    ) {
                        Text(
                            text = "취소",
                            style = MaterialTheme.typography.button,
                            color = MaterialTheme.colors.onSurface
                        )
                    }
                }
            }
        }
    }
}

