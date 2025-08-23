package tools.keystroke.mono.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.Optional
import kotlin.math.roundToInt

enum class DockEdge {
    None, Top, Right, Left,
}

// TODO: use a custom type so we can also render the app icon
data class DockApps(
    val top: Optional<String> = Optional.empty(),
    val left: Optional<String> = Optional.empty(),
    val right: Optional<String> = Optional.empty()
)

private fun launchApp(context: Context, packageName: String) {
    TODO("Not implemented")
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DraggableCircleDock(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    diameter: Dp = 64.dp,
    apps: DockApps = DockApps(),
    threshold: Dp = 96.dp,
    initialOffset: Offset = Offset(24f, 24f),
    onTap: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val containerWidth = with(density) { maxWidth.toPx() }
        val containerHeight = with(density) { maxHeight.toPx() }
        val diameterPx = with(density) { diameter.toPx() }
        val topThresholdPx = with(density) { threshold.toPx() }

        val x = remember { Animatable(initialOffset.x) }
        val y = remember { Animatable(initialOffset.y) }

        fun clamp(value: Float, upperBound: Float): Float {
            return value.coerceIn(0f, upperBound - diameterPx)
        }

        fun determineDockEdge(offset: Offset): DockEdge {
            if (offset.y <= topThresholdPx) return DockEdge.Top

            val distanceToLeft = offset.x
            val distanceToRight = containerWidth - (offset.x + diameterPx)
            return if (distanceToLeft < distanceToRight) {
                DockEdge.Left
            } else {
                DockEdge.Right
            }
        }

        suspend fun snapToEdge(edge: DockEdge) {
            when (edge) {
                DockEdge.Left -> x.animateTo(
                    0f, spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.75f)
                )

                DockEdge.Right -> x.animateTo(
                    containerWidth - diameterPx,
                    spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.75f)
                )

                DockEdge.Top -> y.animateTo(
                    0f, spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.75f)
                )

                DockEdge.None -> {}
            }

            y.animateTo(
                clamp(y.value, containerHeight),
                spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.9f)
            )
            x.animateTo(
                clamp(x.value, containerWidth),
                spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.9f)
            )
        }

        fun onDrag(change: PointerInputChange, drag: Offset) {
            change.consume()
            scope.launch {
                x.snapTo(clamp(x.value + drag.x, containerWidth))
                y.snapTo(clamp(y.value + drag.y, containerHeight))
            }
        }

        fun onDragEnd() {
            val edge = determineDockEdge(Offset(x.value, y.value))
            val app = when (edge) {
                DockEdge.Top -> apps.top
                DockEdge.Left -> apps.left
                DockEdge.Right -> apps.right
                else -> Optional.empty()
            }

            if (app.isEmpty) return;

            scope.launch {
                snapToEdge(edge)
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                launchApp(context, app.get())
            }
        }

        Box(modifier = Modifier
            .offset { IntOffset(x.value.roundToInt(), y.value.roundToInt()) }
            .size(diameter)
            .pointerInput(Unit) {
                detectTapGestures {
                    onTap()
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, drag -> onDrag(change, drag) },
                    onDragEnd = { onDragEnd() },
                )
            }
            .indication(interaction, LocalIndication.current)
            .background(color, CircleShape))
    }
}
