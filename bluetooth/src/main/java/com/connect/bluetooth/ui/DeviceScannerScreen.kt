package com.connect.bluetooth.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun DeviceScannerScreen() {
    var isScanning by remember { mutableStateOf(false) }
    var deviceFound by remember { mutableStateOf(false) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(3000) // Fake scan delay
            deviceFound = true
            isScanning = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Recherche de Montre",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (isScanning) "Recherche en cours..." else if (deviceFound) "Montre détectée !" else "Appuyez pour chercher",
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                RadarAnimation()
            }

            // Central Watch Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (deviceFound) Color(0xFF64FFDA) else Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Text("⌚", fontSize = 40.sp)
            }

            if (deviceFound) {
                // Fake Device Card popping up
                Box(modifier = Modifier.offset(y = 100.dp)) {
                    Column(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), CircleShape)
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Xiaomi Watch S5", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Appuyer pour connecter", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = {
                isScanning = true
                deviceFound = false
            },
            enabled = !isScanning,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF64FFDA),
                contentColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
        ) {
            Text(if (deviceFound) "Relancer la recherche" else "Rechercher", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun RadarAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val radius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFF64FFDA).copy(alpha = alpha),
            radius = radius.dp.toPx(),
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF64FFDA).copy(alpha = alpha * 0.5f),
            radius = (radius * 0.5f).dp.toPx(),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
