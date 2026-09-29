package com.phoenix.launcher.ui.widgets

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoenix.launcher.ui.components.FrostedGlassCard
import com.phoenix.launcher.ui.theme.IosBlue
import com.phoenix.launcher.ui.theme.IosGreen
import com.phoenix.launcher.ui.theme.IosPink

@Composable
fun DeviceHardwareWidget(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val batteryPct = remember(context) { getBatteryLevel(context) }

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hardware Telemetry",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Galaxy S24 Ultra",
                    color = IosGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularStatRing(
                    title = "Battery",
                    value = "$batteryPct%",
                    progress = batteryPct / 100f,
                    ringColor = IosGreen
                )
                CircularStatRing(
                    title = "Storage",
                    value = "64%",
                    progress = 0.64f,
                    ringColor = IosBlue
                )
                CircularStatRing(
                    title = "RAM",
                    value = "6.8G",
                    progress = 0.58f,
                    ringColor = IosPink
                )
            }
        }
    }
}

@Composable
fun CircularStatRing(
    title: String,
    value: String,
    progress: Float,
    ringColor: Color,
    size: Dp = 56.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = 5.dp.toPx()
                // Track background
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    style = Stroke(width = strokeWidth)
                )
                // Animated progress arc
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = value,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getBatteryLevel(context: Context): Int {
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
    val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    return if (level >= 0 && scale > 0) {
        (level * 100 / scale)
    } else {
        88
    }
}
