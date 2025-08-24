package tools.keystroke.mono.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tools.keystroke.mono.utils.AppManager
import java.util.Optional

data class RingConfig(
    val size: Dp,
    val thickness: Dp,
    val color: Color,
    val alpha: Float = 1f,
    // The spacing between each item in the dock
    val itemSpacing: Dp = 24.dp,
)

data class FavoriteApps(
    val left: Optional<String> = Optional.empty(),
    val right: Optional<String> = Optional.empty(),
)

@Composable
fun Dock(
    modifier: Modifier = Modifier,
    height: Dp = 150.dp,
    config: RingConfig = RingConfig(
        size = 64.dp,
        thickness = 5.dp,
        color = MaterialTheme.colorScheme.onBackground,
    ),
    favoriteApps: FavoriteApps = FavoriteApps(),
    onOpenDrawer: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var lastLaunchedApp by remember { mutableStateOf<String?>(null) }
    var tapJob by remember { mutableStateOf<Job?>(null) }

    Row(
        modifier = modifier.padding(bottom = height - config.size),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(config.itemSpacing)
    ) {
        if (favoriteApps.left.isPresent) {
            DockApp(
                packageName = favoriteApps.left.get(),
                setLastLaunched = { pkg -> lastLaunchedApp = pkg })
        }

        Surface(
            color = Color.Transparent,
            shape = CircleShape,
            border = BorderStroke(config.thickness, config.color),
            modifier = Modifier
                .size(config.size)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    tapJob?.cancel()
                    tapJob = scope.launch {
                        delay(100)
                        onOpenDrawer()
                    }
                }) {}

        if (favoriteApps.right.isPresent) {
            DockApp(
                packageName = favoriteApps.right.get(),
                setLastLaunched = { pkg -> lastLaunchedApp = pkg })
        }
    }
}

@Composable
fun DockApp(
    packageName: String,
    setLastLaunched: (String) -> Unit,
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    val appLabel = AppManager.getAppLabel(context, packageName)

    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .padding(end = 12.dp)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (AppManager.launchApp(context, packageName).isSuccess) {
                    setLastLaunched(packageName)
                }
            }, contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = appLabel.getOrDefault(""),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
