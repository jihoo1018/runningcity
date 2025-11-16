package com.runningcity.presentation

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.runningcity.presentation.theme.RunningcityTheme
import com.runningcity.presentation.theme.accentGreen
import com.runningcity.utils.MobileCommunicationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 워치 연동 화면
 * 모바일에서 연동 요청 시 워치에 표시되는 Activity
 */
class WatchPairingActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 화면 켜기 및 잠금 해제
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        
        setContent {
            RunningcityTheme {
                WatchPairingScreen(
                    onConfirm = {
                        // 모바일로 연동 완료 메시지 전송
                        CoroutineScope(Dispatchers.IO).launch {
                            MobileCommunicationHelper.sendWatchPaired(applicationContext)
                        }
                        finish()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun WatchPairingScreen(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단 여백
            Spacer(Modifier.height(8.dp))
            
            // 메인 콘텐츠
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 체크 아이콘
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "연결 완료",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colors.accentGreen
                )
                
                Spacer(Modifier.height(16.dp))
                
                // 연결 메시지
                Text(
                    text = "연결이\n되었습니다",
                    style = MaterialTheme.typography.body1.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground
                )
            }
            
            // 하단 버튼 영역
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .width(80.dp)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    )
                ) {
                    Text(
                        text = "확인",
                        style = MaterialTheme.typography.button
                    )
                }
            }
            
            // 하단 여백
            Spacer(Modifier.height(8.dp))
        }
    }
}

