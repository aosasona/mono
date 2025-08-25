package tools.keystroke.mono.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.net.toUri

data class AppInfo(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean
)

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
            return try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                    ?: return Result.failure(Exception("No launch intent found for package: $packageName"))
                context.startActivity(launchIntent)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun isAppInstalled(context: Context, packageName: String): Boolean {
            return try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: Exception) {
                false
            }
        }

        fun getInstalledApps(context: Context): List<AppInfo> {
            val packageManager = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)

            return activities.mapNotNull { activity ->
                val info = activity.activityInfo?.applicationInfo ?: return@mapNotNull null
                val label = packageManager.getApplicationLabel(info).toString()
                val isSystemApp = (info.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                AppInfo(
                    packageName = info.packageName,
                    label = label,
                    isSystemApp = isSystemApp
                )
            }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
        }

        fun openAppInfoSettings(context: Context, packageName: String): Result<Unit> {
            return try {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:$packageName".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun requestUninstall(context: Context, packageName: String): Result<Unit> {
            return try {
                val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
                    data = "package:$packageName".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                context.startActivity(uninstallIntent)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
