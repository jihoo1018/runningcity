package com.runningcity.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.runningcity.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Typography
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Shapes

@Composable
fun RunningcityTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColorPalette,
        typography = AppTypography,
        shapes = AppShapes,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.background)
        ) {
            content()
        }
    }
}

private val DarkColorPalette = Colors(
    primary = Color(0xFF00E6FF),
    background = Color(0xFF13161C),
    onPrimary = Color(0xFFE6FFFF),
    onBackground = Color(0xFF94B8B8),

    surface = Color(0x1400E6FF),
    onSurface = Color(0xFFE6F2F2),
    error = Color(0xFFFF4D4D),
    onError = Color(0xFFFFFFFF),
)

val Colors.accentRed: Color
    get() = Color(0xFFFF4935)

val Colors.accentGreen: Color
    get() = Color(0xFF00E699)

val Colors.accentBlue: Color
    get() = Color(0xFFB447EB)

val Colors.accentOrange: Color
    get() = Color(0xFFFF8C1A)

private val AppFontFamily = FontFamily(
    Font(R.font.galmuri11, FontWeight.Normal),
    Font(R.font.galmuri11_bold, FontWeight.Bold)
)

private val BaseTypography = Typography()

private val AppTypography = BaseTypography.copy(
    display1 = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
    ),
    display2 = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 32.sp,
    ),
    title1 = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    title2 = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    body1 = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    body2 = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    ),
    button = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    caption1 = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)