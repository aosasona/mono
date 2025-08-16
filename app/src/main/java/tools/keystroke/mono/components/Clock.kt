package tools.keystroke.mono.components

import android.provider.Settings
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import tools.keystroke.mono.ui.theme.VT323

@Composable
fun rememberIs24HourFormat(): Boolean {
    val context = LocalContext.current
    return remember {
        val systemTimeFormat =
                Settings.System.getString(context.contentResolver, Settings.System.TIME_12_24)
        when (systemTimeFormat) {
            "24" -> true
            "12" -> false
            else -> {
                // Default to 24-hour format
                true
            }
        }
    }
}

@Composable
fun Clock(
        modifier: Modifier = Modifier,
        zoneId: ZoneId = ZoneId.systemDefault(),
        use24HourFormat: Boolean = rememberIs24HourFormat(),
        showDate: Boolean = true,
        showSeconds: Boolean = true
) {
    var now by remember { mutableStateOf(ZonedDateTime.now(zoneId)) }
    LaunchedEffect(zoneId) {
        while (true) {
            val currentTime = ZonedDateTime.now(zoneId)
            now = currentTime
            val delayMs =
                    if (showSeconds) {
                        1_000L - (System.currentTimeMillis() % 1_000L)
                    } else {
                        // Roughly after the minute mark to ensure the clock updates at the start of
                        // the next minute
                        60_000L - (System.currentTimeMillis() % 60_000L) + 60L
                    }
            delay(delayMs)
        }
    }

    val currentTime =
            remember(use24HourFormat, showSeconds) {
                var format = if (use24HourFormat) "HH:mm" else "hh:mm"
                format += if (showSeconds) ":ss" else " a"
                DateTimeFormatter.ofPattern(format, Locale.getDefault())
            }

    val currentDate = remember {
        DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.getDefault())
    }

    Column(horizontalAlignment = Alignment.Start) {
        Text(
                text = now.format(currentTime),
                modifier = modifier,
                style =
                        TextStyle(
                                fontFamily = VT323,
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onBackground,
                        )
        )
    }
}
