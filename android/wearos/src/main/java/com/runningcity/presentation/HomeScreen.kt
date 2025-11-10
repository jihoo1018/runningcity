package com.runningcity.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.runningcity.presentation.theme.RunningcityTheme
import com.runningcity.R
import com.runningcity.utils.PermissionManager  // ✅ import 추가!

//@Preview(
//    name = "RunningCity Theme Preview",
//    device = "id:wearos_small_round",
//    showSystemUi = true,
//    backgroundColor = 0xFF13161C,
//    showBackground = true
//)
@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit  // ✅ 파라미터는 이것만!
) {
    RunningcityTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onBackground
                )

                Spacer(Modifier.height(16.dp))

                val shape: Shape = MaterialTheme.shapes.small
                val btnBg = MaterialTheme.colors.surface
                val borderColor = MaterialTheme.colors.primary
                val contentColor = MaterialTheme.colors.onPrimary

                Button(
                    onClick = onStartWorkout,
                    shape = shape,
                    modifier = Modifier
                        .widthIn(min = 100.dp)
                        .height(58.dp)
                        .border(1.dp, borderColor, shape),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = btnBg,
                        contentColor = contentColor
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.run_button),
                            style = MaterialTheme.typography.display1,
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.DirectionsRun,
                            contentDescription = "Run start"
                        )
                    }
                }
            }
        }
    }
}