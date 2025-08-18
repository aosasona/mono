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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

data class ConnectionState(
    val hasInternetAccess: Boolean, val strength: Int = 0
)

@Composable
fun rememberConnectionState(context: Context): ConnectionState {
    var state by remember {
        mutableStateOf(
            ConnectionState(hasInternetAccess = false, strength = 0)
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
            return ConnectionState(hasInternetAccess = hasInternetAccess, strength = 0)
        }

        val wifiInfo = capabilities.transportInfo as WifiInfo?
        val rssi = wifiInfo?.rssi
        return ConnectionState(
            hasInternetAccess = hasInternetAccess, strength = when {
                rssi == null -> 0
                rssi >= -50 -> 4
                rssi >= -60 -> 3
                rssi >= -70 -> 2
                rssi >= -80 -> 1
                else -> 0
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
fun ConnectionStateIndicator(
    height: Dp = 12.dp,
    state: ConnectionState,
    modifier: Modifier = Modifier,
) {
    if (!state.hasInternetAccess) {
        return Row {
            Text(
                text = "No Internet",
                modifier = modifier,
                color = MaterialTheme.colorScheme.error,
                fontFamily = mozillaTextFamily,
                fontWeight = FontWeight.Light,
                fontSize = (height * 0.75f).value.sp
            )
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        for (i in 1..4) {
            val active = i <= state.strength
            val size = height * 0.75f
            Box(
                Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )
            )
        }
    }
}


@Composable
fun HomeStatusBar(
    height: Dp = 20.dp,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    connectionState: ConnectionState = rememberConnectionState(context = context),
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height + 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConnectionStateIndicator(
            height = height,
            state = connectionState,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeStatusBarPreview() {
    Column(
        modifier = Modifier.padding(paddingValues = PaddingValues(16.dp)),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        HomeStatusBar(
            modifier = Modifier.fillMaxWidth(), connectionState = ConnectionState(
                hasInternetAccess = false, strength = 0
            )
        )
        Spacer(modifier = Modifier)

        HomeStatusBar(
            modifier = Modifier.fillMaxWidth(), connectionState = ConnectionState(
                hasInternetAccess = true, strength = 3
            )
        )
        Spacer(modifier = Modifier)

        HomeStatusBar(
            modifier = Modifier.fillMaxWidth(), connectionState = ConnectionState(
                hasInternetAccess = true, strength = 4
            )
        )
        Spacer(modifier = Modifier)
    }
}
