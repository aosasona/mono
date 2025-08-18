package tools.keystroke.mono.components

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import tools.keystroke.mono.ui.theme.mozillaTextFamily
import kotlin.math.max
import kotlin.math.min

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

    clockSize: Int = 120,
    dateSize: Int = 22,
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


    Column(horizontalAlignment = Alignment.Start, modifier = modifier.fillMaxWidth()) {
        if (showDate) {
            Text(
                text = currentDate.format(now),
                style =
                    TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = mozillaTextFamily,
                        fontSize = dateSize.sp,
                        fontWeight = FontWeight.ExtraLight,
                        textAlign = TextAlign.Start,
                    )
            )
        }

        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .offset(y = (-(dateSize * .5f)).dp)
        ) {
            Text(
                text = currentTime.format(now),
                maxLines = 1,
                softWrap = false,
                style =
                    TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = mozillaTextFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = clockSize.sp,
                        textAlign = TextAlign.Start,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Proportional,
                            trim = LineHeightStyle.Trim.Both
                        )
                    )
            )

            if (!use24HourFormat) {
                Text(
                    text = DateTimeFormatter.ofPattern("a", Locale.getDefault()).format(now),
                    modifier = Modifier.padding(top = 16.dp, start = 4.dp),
                    style =
                        TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = mozillaTextFamily,
                            fontSize = (clockSize / 3).sp,
                            fontWeight = FontWeight.Light,
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
