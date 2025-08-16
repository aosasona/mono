package tools.keystroke.mono.views

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import tools.keystroke.mono.components.Clock
import tools.keystroke.mono.ui.theme.MonoTheme

@Composable
fun HomeView() {
    Column {
        // TODO: add status bar here

        Clock()
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    MonoTheme { HomeView() }
}
