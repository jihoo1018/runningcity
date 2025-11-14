package com.runningcity.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme


@Composable
fun Divider(
    color: Color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
    height: Dp = 2.dp,
    padding: Dp = 10.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = padding)
            .height(height)
            .background(color)
    )
}