package com.phoenix.launcher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.phoenix.launcher.model.AppInfo

@Composable
fun AppActionSheet(
    app: AppInfo?,
    isInDock: Boolean = false,
    onDismiss: () -> Unit,
    onToggleDock: (AppInfo) -> Unit = {},
    onToggleHide: (AppInfo) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    onUninstall: (AppInfo) -> Unit
) {
    if (app == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        FrostedGlassCard(
            modifier = Modifier
                .width(280.dp)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with App Icon and Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (app.icon != null) {
                        val bitmap = remember(app.icon) {
                            try {
                                app.icon.toBitmap(120, 120)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = app.label,
                                modifier = Modifier.size(46.dp)
                            )
                        } else {
                            DefaultAppPlaceholder(label = app.label, size = 46.dp)
                        }
                    } else {
                        DefaultAppPlaceholder(label = app.label, size = 46.dp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.label,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (app.isHidden) "Hidden App" else app.category.title,
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.7.dp)
                Spacer(modifier = Modifier.height(6.dp))

                // Action 1: Remove from Dock / Add to Dock
                ActionRow(
                    icon = if (isInDock) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                    label = if (isInDock) "Remove from Dock" else "Add to Dock",
                    tint = if (isInDock) Color(0xFFFF453A) else Color(0xFF34C759),
                    onClick = { onToggleDock(app) }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)

                // Action 2: Hide / Unhide
                ActionRow(
                    icon = if (app.isHidden) Icons.Default.Visibility else Icons.Default.Lock,
                    label = if (app.isHidden) "Unhide App" else "Hide App",
                    tint = if (app.isHidden) Color(0xFF34C759) else Color(0xFFFF9F0A),
                    onClick = { onToggleHide(app) }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)

                // Action 3: App Info
                ActionRow(
                    icon = Icons.Default.Info,
                    label = "App Info",
                    tint = Color.White,
                    onClick = { onAppInfo(app) }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)

                // Action 4: Uninstall
                ActionRow(
                    icon = Icons.Default.Delete,
                    label = "Uninstall",
                    tint = Color(0xFFFF453A),
                    onClick = { onUninstall(app) }
                )
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = tint,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
