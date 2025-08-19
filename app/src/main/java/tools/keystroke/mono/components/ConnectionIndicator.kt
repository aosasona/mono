package tools.keystroke.mono.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tools.keystroke.mono.ui.theme.mozillaTextFamily

data class ConnectionState(
    val hasInternetAccess: Boolean, val strength: Int = 0
)

// TODO: represent "Not connected" state (currently, we just have "No Internet" state)
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
    height: Dp = 20.dp,
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
                Modifier.size(size).clip(CircleShape).background(
                    if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    )
                )
            )
        }
    }
}

