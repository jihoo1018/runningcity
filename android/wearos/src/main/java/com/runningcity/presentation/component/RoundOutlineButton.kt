package com.runningcity.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme

@Composable
fun RoundOutlineButton(
    onClick: () -> Unit,
    icon: ImageVector,
    size: Dp = 52.dp,
    iconDesc: String,
    bgColor: Color = MaterialTheme.colors.surface,
    iconColor: Color = MaterialTheme.colors.onPrimary,
    border: Boolean = true
) {
    val shape = CircleShape
    var borderColor = bgColor
    if (border) {
        borderColor = MaterialTheme.colors.primary
    }
    val bg = bgColor
    val contentColor = iconColor

    Button(
        onClick = onClick,
        shape = shape,
        modifier = Modifier
            .size(size)
            .border(1.dp, borderColor, shape),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = bg,
            contentColor = contentColor
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconDesc,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )
    }
}