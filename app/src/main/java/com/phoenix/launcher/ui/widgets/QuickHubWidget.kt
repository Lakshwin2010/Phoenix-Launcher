package com.phoenix.launcher.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoenix.launcher.ui.components.FrostedGlassCard
import com.phoenix.launcher.ui.theme.IosBlue
import com.phoenix.launcher.ui.theme.IosIndigo
import com.phoenix.launcher.ui.theme.IosOrange

@Composable
fun QuickHubWidget(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    var wifiEnabled by remember { mutableStateOf(true) }
    var btEnabled by remember { mutableStateOf(true) }
    var torchEnabled by remember { mutableStateOf(false) }
    var dndEnabled by remember { mutableStateOf(false) }

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Control Hub",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickHubTile(
                    title = "Wi-Fi",
                    icon = Icons.Default.Wifi,
                    isActive = wifiEnabled,
                    activeColor = IosBlue,
                    onToggle = { wifiEnabled = !wifiEnabled }
                )
                QuickHubTile(
                    title = "Bluetooth",
                    icon = Icons.Default.Bluetooth,
                    isActive = btEnabled,
                    activeColor = IosBlue,
                    onToggle = { btEnabled = !btEnabled }
                )
                QuickHubTile(
                    title = "Torch",
                    icon = Icons.Default.FlashlightOn,
                    isActive = torchEnabled,
                    activeColor = IosOrange,
                    onToggle = { torchEnabled = !torchEnabled }
                )
                QuickHubTile(
                    title = "Focus",
                    icon = Icons.Default.DarkMode,
                    isActive = dndEnabled,
                    activeColor = IosIndigo,
                    onToggle = { dndEnabled = !dndEnabled }
                )
            }
        }
    }
}

@Composable
fun QuickHubTile(
    title: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onToggle: () -> Unit
) {
    val cornerShape = RoundedCornerShape(16.dp)
    val bg = if (isActive) activeColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f)
    val borderCol = if (isActive) activeColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f)
    val iconTint = if (isActive) activeColor else Color.White.copy(alpha = 0.5f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(cornerShape)
            .clickable(onClick = onToggle)
            .background(bg)
            .border(1.dp, borderCol, cornerShape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = Color.White.copy(alpha = if (isActive) 0.95f else 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
