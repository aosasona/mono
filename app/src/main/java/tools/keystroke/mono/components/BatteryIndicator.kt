package tools.keystroke.mono.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

@Composable
fun BatteryIndicator(
    modifier: Modifier = Modifier,
    state: BatteryState,
    height: Dp = 20.dp,
    showPercentage: Boolean = true,
) {
    TODO("NOT IMPLEMENTED")
}
