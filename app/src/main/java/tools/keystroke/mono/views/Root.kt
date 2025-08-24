package tools.keystroke.mono.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tools.keystroke.mono.components.HomeStatusBar

@Composable
fun Root(paddingValues: PaddingValues) {
    val navController = rememberNavController()

    Column {
        HomeStatusBar(modifier = Modifier.padding(horizontal = 16.dp))

        NavHost(navController = navController, startDestination = "home") {
            composable("home") { HomeView(paddingValues, navController = navController) }
            composable("app_drawer") { AppDrawer(paddingValues) }
        }
    }
}
