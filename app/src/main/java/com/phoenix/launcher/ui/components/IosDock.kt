package com.phoenix.launcher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoenix.launcher.model.AppInfo
import com.phoenix.launcher.ui.theme.DockGlassBackground
import com.phoenix.launcher.ui.theme.GlassBorderDark

@Composable
fun IosDock(
    dockApps: List<AppInfo>,
    modifier: Modifier = Modifier,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit = {}
) {
    var loopOffset by remember { mutableIntStateOf(0) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    var slideDirection by remember { mutableIntStateOf(1) }

    val totalCount = dockApps.size
    val safeOffset = if (totalCount == 0) 0 else loopOffset % totalCount
    val visibleCount = minOf(4, totalCount)

    // Circular loop of apps currently visible in the dock slots
    val displayedApps = if (totalCount == 0) emptyList() else {
        (0 until visibleCount).map { i ->
            dockApps[(safeOffset + i) % totalCount]
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        FrostedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        dragAccumulator += delta
                        if (dragAccumulator < -35f) {
                            if (totalCount > 1) {
                                slideDirection = 1
                                loopOffset = (loopOffset + 1) % totalCount
                            }
                            dragAccumulator = 0f
                        } else if (dragAccumulator > 35f) {
                            if (totalCount > 1) {
                                slideDirection = -1
                                loopOffset = (loopOffset - 1 + totalCount) % totalCount
                            }
                            dragAccumulator = 0f
                        }
                    },
                    onDragStopped = { velocity ->
                        if (velocity < -250f && totalCount > 1) {
                            slideDirection = 1
                            loopOffset = (loopOffset + 1) % totalCount
                        } else if (velocity > 250f && totalCount > 1) {
                            slideDirection = -1
                            loopOffset = (loopOffset - 1 + totalCount) % totalCount
                        }
                        dragAccumulator = 0f
                    }
                ),
            shape = RoundedCornerShape(34.dp),
            backgroundColor = DockGlassBackground,
            borderColor = GlassBorderDark,
            elevation = 12.dp
        ) {
            if (dockApps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Long-press any app to Add to Dock",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = safeOffset,
                        transitionSpec = {
                            if (slideDirection > 0) {
                                (slideInHorizontally(animationSpec = tween(180)) { width -> width / 3 } + fadeIn(tween(180)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(180)) { width -> -width / 3 } + fadeOut(tween(180)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(180)) { width -> -width / 3 } + fadeIn(tween(180)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(180)) { width -> width / 3 } + fadeOut(tween(180)))
                            }
                        },
                        label = "DockLoopAnimation"
                    ) { _ ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            displayedApps.forEach { app ->
                                SquircleIcon(
                                    app = app,
                                    iconSize = 56.dp,
                                    showLabel = false,
                                    onClick = { onAppClick(app) },
                                    onLongClick = { onAppLongClick(app) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
