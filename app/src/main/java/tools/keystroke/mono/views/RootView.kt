package tools.keystroke.mono.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tools.keystroke.mono.components.Clock
import tools.keystroke.mono.components.HomeStatusBar
import tools.keystroke.mono.ui.theme.MonoTheme

@Composable
// TODO: load user settings
fun HomeView(
    paddingValues: PaddingValues, modifier: Modifier = Modifier
) {
    val paddingModifier = Modifier.padding(horizontal = 14.dp)

    Column {
        HomeStatusBar(modifier = paddingModifier)

        Spacer(modifier = Modifier.padding(10.dp))

        Clock(modifier = paddingModifier)

        Spacer(modifier = Modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    MonoTheme { HomeView(paddingValues = PaddingValues(16.dp)) }
}
