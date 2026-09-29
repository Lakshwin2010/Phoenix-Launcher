package com.phoenix.launcher.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.phoenix.launcher.LauncherScreen
import com.phoenix.launcher.LauncherViewModel
import com.phoenix.launcher.model.AppCategory
import com.phoenix.launcher.model.AppInfo
import com.phoenix.launcher.ui.components.DefaultAppPlaceholder
import com.phoenix.launcher.ui.components.FrostedGlassCard
import com.phoenix.launcher.ui.components.SquircleIcon

@Composable
fun AppLibraryScreen(
    viewModel: LauncherViewModel,
    visibleApps: List<AppInfo>,
    hiddenApps: List<AppInfo>,
    allApps: List<AppInfo>,
    isVaultUnlocked: Boolean,
    onOpenVault: () -> Unit,
    onLockVault: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAppPicker by remember { mutableStateOf(false) }

    val categories = listOf(
        AppCategory.SOCIAL,
        AppCategory.CREATIVITY,
        AppCategory.PRODUCTIVITY,
        AppCategory.UTILITIES,
        AppCategory.GAMES,
        AppCategory.OTHER
    )

    // Pre-filter so only categories with visible apps are assigned slots in the grid.
    // This prevents empty/blank items from taking up cells in LazyVerticalGrid.
    val activeCategories = remember(visibleApps) {
        categories.filter { category ->
            visibleApps.any { it.category == category }
        }
    }

    // Top 4 apps for iOS Suggestions folder
    val suggestionApps = remember(visibleApps) {
        val topByLaunch = visibleApps.filter { it.launchCount > 0 }.sortedByDescending { it.launchCount }
        if (topByLaunch.size >= 4) {
            topByLaunch.take(4)
        } else {
            (topByLaunch + visibleApps.filterNot { topByLaunch.contains(it) }).take(4)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Header with Back arrow returning to Home and App Library title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "App Library",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search pill leading to Spotlight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { viewModel.navigateTo(LauncherScreen.SPOTLIGHT) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Search all apps…",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2-Column Grid of iOS Folders + Hidden Folder (Identical square folder layout)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. iOS Suggestions Folder
                if (suggestionApps.isNotEmpty()) {
                    item {
                        IosCategoryFolderCard(
                            categoryTitle = "Suggestions",
                            apps = suggestionApps,
                            onAppClick = onAppClick,
                            onAppLongClick = onAppLongClick
                        )
                    }
                }

                // 2. Active Category Folders
                items(activeCategories) { category ->
                    val categoryApps = visibleApps.filter { it.category == category }
                    IosCategoryFolderCard(
                        categoryTitle = category.title,
                        apps = categoryApps,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick
                    )
                }

                // 3. iOS 18 "Hidden" Folder (Normal 1-column folder card)
                item {
                    IosHiddenFolderCard(
                        isUnlocked = isVaultUnlocked,
                        hiddenApps = hiddenApps,
                        onUnlockClick = onOpenVault,
                        onLockClick = onLockVault,
                        onAddClick = { showAppPicker = true },
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick
                    )
                }
            }
        }

        // Modal to pick apps to hide / unhide
        if (showAppPicker) {
            HideAppsPickerDialog(
                allApps = allApps,
                onDismiss = { showAppPicker = false },
                onToggleHide = { app ->
                    if (app.isHidden) {
                        viewModel.unhideApp(app)
                    } else {
                        viewModel.hideApp(app)
                    }
                }
            )
        }
    }
}

@Composable
fun IosHiddenFolderCard(
    isUnlocked: Boolean,
    hiddenApps: List<AppInfo>,
    onUnlockClick: () -> Unit,
    onLockClick: () -> Unit,
    onAddClick: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        onClick = if (!isUnlocked) onUnlockClick else null
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            if (!isUnlocked) {
                // Locked State: Clean, centered iOS-style lock in 112.dp container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Hidden Vault Locked",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hidden",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(11.dp)
                    )
                }
            } else {
                // Unlocked State: Exact same 2x2 grid layout as normal category folder cards
                if (hiddenApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable(onClick = onAddClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add apps",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add Apps",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    when (hiddenApps.size) {
                        1 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(112.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                MiniAppSlot(hiddenApps[0], onAppClick, onAppLongClick)
                            }
                        }
                        2 -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(112.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MiniAppSlot(hiddenApps[0], onAppClick, onAppLongClick)
                                MiniAppSlot(hiddenApps[1], onAppClick, onAppLongClick)
                            }
                        }
                        3 -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MiniAppSlot(hiddenApps[0], onAppClick, onAppLongClick)
                                    MiniAppSlot(hiddenApps[1], onAppClick, onAppLongClick)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MiniAppSlot(hiddenApps[2], onAppClick, onAppLongClick)
                                }
                            }
                        }
                        else -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MiniAppSlot(hiddenApps[0], onAppClick, onAppLongClick)
                                    MiniAppSlot(hiddenApps[1], onAppClick, onAppLongClick)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MiniAppSlot(hiddenApps[2], onAppClick, onAppLongClick)
                                    if (hiddenApps.size > 4) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+${hiddenApps.size - 3}",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        MiniAppSlot(hiddenApps[3], onAppClick, onAppLongClick)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Hidden",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable(onClick = onAddClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable(onClick = onLockClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IosCategoryFolderCard(
    categoryTitle: String,
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Adaptive Mini App Grid inside folder: NEVER renders blank black holes
            when (apps.size) {
                1 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MiniAppSlot(apps[0], onAppClick, onAppLongClick)
                    }
                }
                2 -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiniAppSlot(apps[0], onAppClick, onAppLongClick)
                        MiniAppSlot(apps[1], onAppClick, onAppLongClick)
                    }
                }
                3 -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MiniAppSlot(apps[0], onAppClick, onAppLongClick)
                            MiniAppSlot(apps[1], onAppClick, onAppLongClick)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            MiniAppSlot(apps[2], onAppClick, onAppLongClick)
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MiniAppSlot(apps[0], onAppClick, onAppLongClick)
                            MiniAppSlot(apps[1], onAppClick, onAppLongClick)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MiniAppSlot(apps[2], onAppClick, onAppLongClick)
                            if (apps.size > 4) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${apps.size - 3}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                MiniAppSlot(apps[3], onAppClick, onAppLongClick)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = categoryTitle,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MiniAppSlot(
    app: AppInfo?,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    if (app != null) {
        SquircleIcon(
            app = app,
            iconSize = 52.dp,
            showLabel = false,
            onClick = { onAppClick(app) },
            onLongClick = { onAppLongClick(app) }
        )
    } else {
        Box(modifier = Modifier.size(52.dp))
    }
}

@Composable
fun HideAppsPickerDialog(
    allApps: List<AppInfo>,
    onDismiss: () -> Unit,
    onToggleHide: (AppInfo) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        FrostedGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(480.dp)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hide Apps",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "Selected apps will only appear in the Hidden Vault.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(allApps) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleHide(app) }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (app.icon != null) {
                                val bitmap = remember(app.icon) {
                                    try {
                                        app.icon.toBitmap(80, 80)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = app.label,
                                        modifier = Modifier.size(36.dp)
                                    )
                                } else {
                                    DefaultAppPlaceholder(label = app.label, size = 36.dp)
                                }
                            } else {
                                DefaultAppPlaceholder(label = app.label, size = 36.dp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = app.label,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Checkbox(
                                checked = app.isHidden,
                                onCheckedChange = { onToggleHide(app) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF34C759),
                                    uncheckedColor = Color.White.copy(alpha = 0.4f),
                                    checkmarkColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
