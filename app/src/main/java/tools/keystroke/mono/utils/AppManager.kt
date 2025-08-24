package tools.keystroke.mono.utils

import android.content.Context
import android.graphics.drawable.Drawable

class AppManager {
    companion object {
        fun getAppLabel(context: Context, packageName: String): Result<String> {
            return try {
                val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                Result.success(context.packageManager.getApplicationLabel(appInfo).toString())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun getAppIcon(context: Context, packageName: String): Result<Drawable> {
            return try {
                val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                Result.success(context.packageManager.getApplicationIcon(appInfo))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun launchApp(context: Context, packageName: String): Result<Unit> {
            TODO("Not implemented yet")
//            return try {
//                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
//                    ?: return Result.failure(Exception("No launch intent found for package: $packageName"))
//                context.startActivity(launchIntent)
//                Result.success(Unit)
//            } catch (e: Exception) {
//                Result.failure(e)
//            }
        }
    }
}
