package tools.keystroke.mono.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun HomeStatusBar(
    height: Dp = 20.dp,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    connectionState: ConnectionState = rememberConnectionState(context = context),
    batteryState: BatteryState = rememberBatteryLevel(context = context),
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height + 28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConnectionStateIndicator(height = height, state = connectionState)

        Spacer(modifier = Modifier.weight(1f))

        BatteryIndicator(height = height, state = batteryState)
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
                isConnected = true, hasInternetAccess = false, strength = 0
            )
        )
        Spacer(modifier = Modifier)

        HomeStatusBar(
            modifier = Modifier.fillMaxWidth(), connectionState = ConnectionState(
                isConnected = true, hasInternetAccess = true, strength = 3
            )
        )
        Spacer(modifier = Modifier)

        HomeStatusBar(
            modifier = Modifier.fillMaxWidth(), connectionState = ConnectionState(
                isConnected = true, hasInternetAccess = true, strength = 4
            )
        )
        Spacer(modifier = Modifier)

        HomeStatusBar(
            modifier = Modifier.fillMaxWidth(), connectionState = ConnectionState(
                isConnected = false, hasInternetAccess = false, strength = 0
            )
        )
        Spacer(modifier = Modifier)
    }
}
