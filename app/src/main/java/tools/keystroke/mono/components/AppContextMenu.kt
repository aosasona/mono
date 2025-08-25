package tools.keystroke.mono.components

import androidx.compose.foundation.border
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tools.keystroke.mono.ui.theme.interFamily

data class ContextMenuItem(
    val title: String,
    val icon: @Composable (() -> Unit)? = null,
    val onClick: () -> Unit,
    val disabled: Boolean = false,
)

@Composable
fun AppContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<ContextMenuItem>,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = 32.dp, y = 0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 1.dp,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.onBackground)
    ) {
        items.forEach { item ->
            DropdownMenuItem(enabled = !item.disabled, text = {
                Text(
                    text = item.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = interFamily
                )
            }, leadingIcon = item.icon, onClick = {
                item.onClick()
                onDismissRequest()
            })
        }
    }
}
