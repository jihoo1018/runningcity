package com.runningcity.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayDateText(
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }

    val formatter = remember {
        DateTimeFormatter.ofPattern("yyyy. M. d. EEEE")
    }

    val formattedDate = remember(today) {
        today.format(formatter)
    }

    Text(
        text = formattedDate,
        style = MaterialTheme.typography.caption1,
        color = MaterialTheme.colors.onBackground,
        modifier = modifier
    )
}