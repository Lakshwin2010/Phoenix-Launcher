package com.phoenix.launcher.model

import android.graphics.drawable.Drawable

enum class AppCategory(val title: String) {
    SUGGESTIONS("Suggestions"),
    SOCIAL("Social & Chat"),
    CREATIVITY("Creativity & Media"),
    UTILITIES("Utilities"),
    PRODUCTIVITY("Productivity"),
    GAMES("Games"),
    OTHER("Other"),
    HIDDEN("Hidden")
}

data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable? = null,
    val category: AppCategory = AppCategory.OTHER,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val launchCount: Int = 0
)
