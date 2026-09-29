package com.phoenix.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserManager
import com.phoenix.launcher.model.AppCategory
import com.phoenix.launcher.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("phoenix_launcher_prefs", Context.MODE_PRIVATE)

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<AppInfo>()
        val pm = context.packageManager
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
        val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
        val userHandle = Process.myUserHandle()

        val favorites = prefs.getStringSet("favorite_packages", emptySet()) ?: emptySet()
        val hiddenPackages = prefs.getStringSet("hidden_packages", emptySet()) ?: emptySet()

        if (launcherApps != null && userManager != null) {
            val activityList = launcherApps.getActivityList(null, userHandle)
            for (activity in activityList) {
                // Ignore Phoenix launcher itself
                if (activity.applicationInfo.packageName == context.packageName) continue

                val label = activity.label.toString()
                val pkgName = activity.applicationInfo.packageName
                val actName = activity.name
                val icon = try {
                    activity.getBadgedIcon(0)
                } catch (e: Exception) {
                    activity.applicationInfo.loadIcon(pm)
                }
                val isHidden = hiddenPackages.contains(pkgName)
                val category = if (isHidden) AppCategory.HIDDEN else determineCategory(activity.applicationInfo, pkgName)
                val isFav = favorites.contains(pkgName)

                apps.add(
                    AppInfo(
                        label = label,
                        packageName = pkgName,
                        activityName = actName,
                        icon = icon,
                        category = category,
                        isFavorite = isFav,
                        isHidden = isHidden
                    )
                )
            }
        } else {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (ri in resolveInfos) {
                if (ri.activityInfo.packageName == context.packageName) continue

                val label = ri.loadLabel(pm).toString()
                val pkgName = ri.activityInfo.packageName
                val actName = ri.activityInfo.name
                val icon = ri.loadIcon(pm)
                val isHidden = hiddenPackages.contains(pkgName)
                val category = if (isHidden) AppCategory.HIDDEN else determineCategory(ri.activityInfo.applicationInfo, pkgName)
                val isFav = favorites.contains(pkgName)

                apps.add(
                    AppInfo(
                        label = label,
                        packageName = pkgName,
                        activityName = actName,
                        icon = icon,
                        category = category,
                        isFavorite = isFav,
                        isHidden = isHidden
                    )
                )
            }
        }

        apps.sortedBy { it.label.lowercase() }
    }

    private fun determineCategory(appInfo: ApplicationInfo, pkg: String): AppCategory {
        val lowerPkg = pkg.lowercase()

        // Social / Communication
        if (lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") ||
            lowerPkg.contains("instagram") || lowerPkg.contains("twitter") ||
            lowerPkg.contains("facebook") || lowerPkg.contains("messenger") ||
            lowerPkg.contains("discord") || lowerPkg.contains("signal") ||
            lowerPkg.contains("messaging") || lowerPkg.contains("dialer") ||
            lowerPkg.contains("phone") || lowerPkg.contains("contact")
        ) {
            return AppCategory.SOCIAL
        }

        // Media / Entertainment / Creativity
        if (lowerPkg.contains("spotify") || lowerPkg.contains("youtube") ||
            lowerPkg.contains("netflix") || lowerPkg.contains("primevideo") ||
            lowerPkg.contains("music") || lowerPkg.contains("gallery") ||
            lowerPkg.contains("camera") || lowerPkg.contains("photos") ||
            lowerPkg.contains("kustom")
        ) {
            return AppCategory.CREATIVITY
        }

        // Productivity
        if (lowerPkg.contains("notes") || lowerPkg.contains("keep") ||
            lowerPkg.contains("docs") || lowerPkg.contains("sheet") ||
            lowerPkg.contains("slides") || lowerPkg.contains("drive") ||
            lowerPkg.contains("calendar") || lowerPkg.contains("mail") ||
            lowerPkg.contains("gmail") || lowerPkg.contains("slack") ||
            lowerPkg.contains("chrome") || lowerPkg.contains("browser")
        ) {
            return AppCategory.PRODUCTIVITY
        }

        // Utilities / System / Tools
        if (lowerPkg.contains("settings") || lowerPkg.contains("clock") ||
            lowerPkg.contains("deskclock") || lowerPkg.contains("calculator") ||
            lowerPkg.contains("calc") || lowerPkg.contains("files") ||
            lowerPkg.contains("document") || lowerPkg.contains("download") ||
            lowerPkg.contains("sec.android") || lowerPkg.contains("samsung") ||
            lowerPkg.contains("vending") || lowerPkg.contains("store") ||
            lowerPkg.contains("stk") || lowerPkg.contains("safetyhub") ||
            lowerPkg.contains("safety") || lowerPkg.contains("maps") ||
            lowerPkg.contains("search") || lowerPkg.contains("googlequicksearchbox")
        ) {
            return AppCategory.UTILITIES
        }

        // System category flag (Android 8+)
        return when (appInfo.category) {
            ApplicationInfo.CATEGORY_GAME -> AppCategory.GAMES
            ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_IMAGE -> AppCategory.CREATIVITY
            ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
            ApplicationInfo.CATEGORY_MAPS -> AppCategory.UTILITIES
            else -> AppCategory.UTILITIES
        }
    }

    fun launchApp(app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName(app.packageName, app.activityName)
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (fallback != null) {
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallback)
            }
        }
    }

    fun toggleFavorite(packageName: String) {
        val currentFavs = prefs.getStringSet("favorite_packages", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (currentFavs.contains(packageName)) {
            currentFavs.remove(packageName)
        } else {
            currentFavs.add(packageName)
        }
        prefs.edit().putStringSet("favorite_packages", currentFavs).apply()
    }

    fun getHiddenPackages(): Set<String> {
        return prefs.getStringSet("hidden_packages", emptySet()) ?: emptySet()
    }

    fun setAppHidden(packageName: String, hidden: Boolean) {
        val currentHidden = prefs.getStringSet("hidden_packages", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (hidden) {
            currentHidden.add(packageName)
        } else {
            currentHidden.remove(packageName)
        }
        prefs.edit().putStringSet("hidden_packages", currentHidden).apply()
    }

    fun getDockPackages(): List<String> {
        val raw = prefs.getString("dock_packages_ordered", null)
        if (raw != null) {
            return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        return emptyList()
    }

    fun setDockPackages(packages: List<String>) {
        prefs.edit().putString("dock_packages_ordered", packages.joinToString(",")).apply()
    }

    fun addDockPackage(packageName: String) {
        val current = getDockPackages().toMutableList()
        if (!current.contains(packageName)) {
            current.add(packageName)
            setDockPackages(current)
        }
    }

    fun removeDockPackage(packageName: String) {
        val current = getDockPackages().toMutableList()
        current.remove(packageName)
        setDockPackages(current)
    }
}
