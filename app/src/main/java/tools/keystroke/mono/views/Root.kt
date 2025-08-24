package tools.keystroke.mono.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Root(paddingValues: PaddingValues) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeView(paddingValues) }
        composable("app_drawer") { AppDrawer(paddingValues) }
    }
}
