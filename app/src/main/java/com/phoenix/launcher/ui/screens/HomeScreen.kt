package com.phoenix.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.phoenix.launcher.LauncherScreen
import com.phoenix.launcher.LauncherViewModel
import com.phoenix.launcher.model.AppInfo
import com.phoenix.launcher.ui.components.DynamicIslandPill
import com.phoenix.launcher.ui.components.IosDock
import com.phoenix.launcher.ui.components.SpotlightSearchBar
import com.phoenix.launcher.ui.components.SquircleIcon
import com.phoenix.launcher.ui.widgets.KwgtSmartStack

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    apps: List<AppInfo>,
    dockApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit = {},
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Home screen grid shows apps that are not currently pinned to the dock
    val homeApps = remember(apps, dockApps) {
        apps.filterNot { dockApp -> dockApps.any { it.packageName == dockApp.packageName } }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Swipe DOWN -> Spotlight search
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 40) {
                        viewModel.navigateTo(LauncherScreen.SPOTLIGHT)
                    }
                }
            }
            .pointerInput(Unit) {
                // Swipe LEFT -> Slide to side App Library
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -40) {
                        onNavigateToLibrary()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // 4-Column App Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                items(homeApps) { app ->
                    SquircleIcon(
                        app = app,
                        iconSize = 58.dp,
                        showLabel = true,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    )
                }
            }

            // iOS Page Dots (Home on left •, App Library on right ◦)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                // Active Page Dot (Home)
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Side Page Dot (App Library) - Clickable to slide to library
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.35f))
                        .clickable(onClick = onNavigateToLibrary)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Frosted Glass Dock with Loop & Long-Press Support
            IosDock(
                dockApps = dockApps,
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
