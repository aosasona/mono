package tools.keystroke.mono.components

import android.os.SystemClock
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    showSeconds: Boolean = false,
    clockSize: Int = 80,
) {
    var now by remember { mutableStateOf(ZonedDateTime.now(zoneId)) }
    LaunchedEffect(zoneId) {
        while (true) {
            val currentTime = ZonedDateTime.now(zoneId)
            now = currentTime
            val delayMs = if (showSeconds) {
                1_000L - (System.currentTimeMillis() % 1_000L)
            } else {
                // Roughly after the minute mark to ensure the clock updates at the start of
                // the next minute
                60_000L - (System.currentTimeMillis() % 60_000L) + 30L
            }
            delay(delayMs)
        }
    }

    val currentTime =
        remember(use24HourFormat, showSeconds) {
            var format = if (use24HourFormat) "HH:mm" else "hh:mm"
            if (showSeconds) {
                format += ":ss"
            }
            DateTimeFormatter.ofPattern(format, Locale.getDefault())
        }

    val currentDate = remember {
        DateTimeFormatter.ofPattern("EE, d MMM yyyy", Locale.getDefault())
    }

    Column(horizontalAlignment = Alignment.Start) {
        if (showDate) {
            Text(
                text = currentDate.format(now),
                modifier = modifier,
                style =
                    TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = VT323,
                        fontSize = (clockSize / 3).sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start,
                    )
            )
        }

        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = currentTime.format(now),
                modifier = modifier,
                style =
                    TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = VT323,
                        fontSize = clockSize.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start,
                    )
            )

            if (!use24HourFormat) {
                Text(
                    text = DateTimeFormatter.ofPattern("a", Locale.getDefault()).format(now),
                    modifier = modifier.padding(top = 12.dp, start = 4.dp),
                    style =
                        TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = VT323,
                            fontSize = (clockSize / 2.5).sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Start,
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClockPreview() {
    Column {
        Clock(use24HourFormat = true)
        Spacer(modifier = Modifier)

        Clock(use24HourFormat = false)
        Spacer(modifier = Modifier)

        Clock(showSeconds = true)
        Spacer(modifier = Modifier)

        Clock(showDate = false)
    }
}
