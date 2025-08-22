package tools.keystroke.mono.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tools.keystroke.mono.ui.theme.InternationalOrange
import tools.keystroke.mono.ui.theme.mozillaTextFamily

data class BatteryState(
    val percentage: Int,
    val isCharging: Boolean,
)

@Composable
fun rememberBatteryLevel(context: Context): BatteryState {
    var state by remember {
        mutableStateOf(BatteryState(percentage = 0, isCharging = false))
    }

    DisposableEffect(Unit) {
        val intent = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
                val percentage = if (level >= 0 && scale > 0) {
                    (level * 100) / scale
                } else {
                    0
                }

                val batteryStatus = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val charging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING
                state = BatteryState(
                    percentage = percentage, isCharging = charging
                )
            }
        }

        context.registerReceiver(receiver, intent)

        // Unregister the receiver when the composable is disposed
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    return state
}

enum class PercentageSide {
    LEFT, RIGHT
}

@Composable
fun BatteryIndicator(
    modifier: Modifier = Modifier,
    state: BatteryState,
    height: Dp = 12.dp,
    width: Dp = (height * 2.4f).coerceAtLeast(40.dp),
    radius: Float = 5f,
    showPercentage: Boolean = true,
    percentageSide: PercentageSide = PercentageSide.RIGHT,
    lowBatteryThreshold: Int = 10,
) {
    val currentPercentage = state.percentage.coerceIn(0, 100)
    val animatedFillPercentage by animateFloatAsState(currentPercentage / 100f)

    val foregroundColor = MaterialTheme.colorScheme.onBackground
    val track = foregroundColor.copy(alpha = 0.25f)
    val fillColor = when {
        state.isCharging -> InternationalOrange
        currentPercentage <= lowBatteryThreshold -> MaterialTheme.colorScheme.error
        else -> foregroundColor
    }


    Row(
        modifier = modifier.height(height),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val percentageIndicator = @Composable {
            Text(
                text = "$currentPercentage%${if (state.isCharging) "+" else ""}",
                color = foregroundColor,
                fontSize = (height.value * 0.8f).sp,
                fontFamily = mozillaTextFamily,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .offset(y = (-height * 0.2f))
            )
        }

        if (showPercentage && percentageSide == PercentageSide.LEFT) {
            percentageIndicator()
        }

        Canvas(
            Modifier
                .width(width)
                .height(height)
        ) {
            val capWidth = size.height * 0.20f
            val capMargin = (size.height * 0.12f)

            drawRoundRect(
                color = track,
                topLeft = Offset.Zero,
                size = Size(size.width - capWidth - capMargin, size.height),
                cornerRadius = CornerRadius(radius, radius)
            )

            val filledWidth = (size.width - capWidth - capMargin) * animatedFillPercentage
            drawRoundRect(
                color = fillColor,
                topLeft = Offset.Zero,
                size = Size(filledWidth, size.height),
                cornerRadius = CornerRadius(radius, radius)
            )

            val capOffset = size.width - capWidth
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(capOffset, size.height * 0.18f),
                size = Size(capWidth, size.height * 0.60f),
                cornerRadius = CornerRadius(radius, radius)
            )
        }

        if (showPercentage && percentageSide == PercentageSide.RIGHT) {
            percentageIndicator()
        }
    }
}
