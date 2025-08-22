package tools.keystroke.mono.components

import android.app.WallpaperManager
import android.content.Context
import android.widget.ImageView
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@RequiresPermission(anyOf = ["android.permission.READ_WALLPAPER_INTERNAL", android.Manifest.permission.MANAGE_EXTERNAL_STORAGE])
@Composable
fun Wallpaper(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
) {
    val wallpaper = remember  {
        WallpaperManager.getInstance(context).drawable
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            ImageView(context).apply {
                setImageDrawable(wallpaper)
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            }
        }
    )
}
