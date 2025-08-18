package tools.keystroke.mono.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

enum class ConnectionStrength {
    EXCELLENT, GOOD, FAIR, POOR, NONE
}

data class ConnectionState(
    val hasInternetAccess: Boolean,
    val strength: ConnectionStrength? = ConnectionStrength.NONE,
)

@Composable
fun rememberConnectionState(context: Context): ConnectionState {
    var state by remember {
        mutableStateOf(
            ConnectionState(hasInternetAccess = false, strength = null)
        )
    }

    fun snapshotState(
        connectivityManager: ConnectivityManager,
    ): ConnectionState {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val hasInternetAccess =
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

        if (capabilities == null || !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return ConnectionState(hasInternetAccess = hasInternetAccess, strength = null)
        }

        val wifiInfo = capabilities.transportInfo as WifiInfo?
        val rssi = wifiInfo?.rssi
        return ConnectionState(
            hasInternetAccess = hasInternetAccess, strength = when {
                rssi == null -> ConnectionStrength.NONE
                rssi >= -50 -> ConnectionStrength.EXCELLENT
                rssi >= -60 -> ConnectionStrength.GOOD
                rssi >= -70 -> ConnectionStrength.FAIR
                else -> ConnectionStrength.POOR
            }
        )
    }

    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        state = snapshotState(connectivityManager)

        val callbacks = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                state = snapshotState(connectivityManager)
            }

            override fun onLost(network: Network) {
                state = snapshotState(connectivityManager)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities,
            ) {
                state = snapshotState(connectivityManager)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callbacks)
    }

    return state
}

@Composable
fun ConnectionStateBar(active: Boolean) {
}

@Composable
fun ConnectionStateIndicator(
    state: ConnectionState,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth()) {
        if (!state.hasInternetAccess) {
            return Text(
                text = "No Internet",
                modifier = modifier,
                color = MaterialTheme.colorScheme.error,
                fontFamily = mozillaTextFamily,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
            )
        }
    }
}


@Composable
fun HomeStatusBar(
    height: Dp = 28.dp,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    connectionState: ConnectionState = rememberConnectionState(context = context),
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConnectionStateIndicator(
            state = connectionState,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeStatusBarPreview() {
    Column(modifier = Modifier.padding(paddingValues = PaddingValues(16.dp))) {
        HomeStatusBar(
            height = 28.dp, modifier = Modifier.fillMaxWidth(), connectionState = ConnectionState(
                hasInternetAccess = false, strength = ConnectionStrength.NONE
            )
        )
        Spacer(modifier = Modifier)

        HomeStatusBar(
            height = 28.dp, modifier = Modifier.fillMaxWidth(), connectionState = ConnectionState(
                hasInternetAccess = true, strength = ConnectionStrength.GOOD
            )
        )
        Spacer(modifier = Modifier)

        HomeStatusBar(
            height = 28.dp, modifier = Modifier.fillMaxWidth(), connectionState = ConnectionState(
                hasInternetAccess = true, strength = ConnectionStrength.EXCELLENT
            )
        )
        Spacer(modifier = Modifier)
    }
}
