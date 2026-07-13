package com.connect.bluetooth.ui

import android.Manifest
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DeviceScannerScreen(
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val isScanning by viewModel.isScanning.collectAsState()
    val deviceFound by viewModel.deviceFound.collectAsState()

    val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        )
    } else {
        rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Pure black like iOS setup screens
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 40.dp)) {
            Text(
                "Jumelage",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Recherche...", // As seen in mockup
                color = Color.Gray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            RadarAnimation()

            // Central iOS styled Watch Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E)),
                contentAlignment = Alignment.Center
            ) {
                 // Simulated hands of the clock
                 Box(modifier = Modifier.width(2.dp).height(30.dp).background(Color.White).align(Alignment.Center).offset(y = (-15).dp))
                 Box(modifier = Modifier.width(30.dp).height(2.dp).background(Color.White).align(Alignment.Center).offset(x = 15.dp, y = (-15).dp))
            }

            // AirPods style popup card from mockup
            Box(modifier = Modifier.offset(y = 100.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .background(Color(0xFF2C2C2E).copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Trouvé", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Xiaomi Watch S5 - Connecté", color = Color.LightGray, fontSize = 13.sp)
                }
            }
        }

        Button(
            onClick = {
                if (bluetoothPermissions.allPermissionsGranted) {
                    viewModel.startScan()
                } else {
                    bluetoothPermissions.launchMultiplePermissionRequest()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF), // Apple Blue
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF2C2C2E),
                disabledContentColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp)), // iOS 15+ button squircle shape
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                "Activer le Bluetooth",
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
fun RadarAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxRadius = size.minDimension / 2f
        val rippleCount = 4

        for (i in 0 until rippleCount) {
            val progress = (phase1 + (i.toFloat() / rippleCount)) % 1f
            val currentRadius = progress * maxRadius

            // Fading out as it grows
            val alpha = (1f - progress) * 0.4f

            drawCircle(
                color = Color(0xFF34C759), // Tinted slightly green as seen in mockup
                radius = currentRadius,
                alpha = alpha,
                style = Stroke(width = 40f * (1f - progress)) // Thick to thin stroke
            )
        }
    }
}
