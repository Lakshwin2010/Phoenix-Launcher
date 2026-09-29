package com.phoenix.launcher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun IosPasscodeDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onValidatePin: (String) -> Boolean
) {
    if (!isOpen) return

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isOpen) {
        enteredPin = ""
        isError = false
    }

    fun handleDigit(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin

            if (newPin.length == 4) {
                val valid = onValidatePin(newPin)
                if (valid) {
                    onSuccess()
                } else {
                    isError = true
                    coroutineScope.launch {
                        shakeOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 400
                                -20f at 50
                                20f at 100
                                -15f at 150
                                15f at 200
                                -8f at 250
                                8f at 300
                                0f at 350
                            }
                        )
                        enteredPin = ""
                        isError = false
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // Stop propagation so clicking inside dialog does not dismiss
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            // Dismiss button
            Row(
                modifier = Modifier.width(300.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // iOS Lock Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Hidden Vault",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter Passcode",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isError) "Incorrect Passcode. Try 1234" else "Unlock Hidden Apps (Default: 1234)",
                color = if (isError) Color(0xFFFF453A) else Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 4-Dot Passcode Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            ) {
                repeat(4) { index ->
                    val isFilled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (isError) Color(0xFFFF453A)
                                else if (isFilled) Color.White
                                else Color.Transparent
                            )
                            .background(
                                if (!isFilled) Color.White.copy(alpha = 0.25f)
                                else Color.Transparent
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // iOS Style Numeric Keypad
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in keyRows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        for (key in row) {
                            when (key) {
                                "" -> {
                                    Spacer(modifier = Modifier.size(72.dp))
                                }
                                "DEL" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable(onClick = { handleBackspace() }),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Delete",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.16f))
                                            .clickable(onClick = { handleDigit(key) }),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            color = Color.White,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
