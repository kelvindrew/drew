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
import androidx.compose.material3.MaterialTheme
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
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                if (isScanning) "Recherche d'appareils à proximité..."
                else if (deviceFound) "Appareil détecté."
                else "Approchez votre montre de l'iPhone.",
                color = Color.Gray,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                RadarAnimation()
            }

            // Central iOS styled Watch Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E)),
                contentAlignment = Alignment.Center
            ) {
                Text("⌚️", fontSize = 48.sp)
            }

            if (deviceFound) {
                // AirPods style popup card
                Box(modifier = Modifier.offset(y = 120.dp)) {
                    Column(
                        modifier = Modifier
                            .background(Color(0xFF1C1C1E), RoundedCornerShape(20.dp))
                            .padding(horizontal = 32.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Xiaomi Watch S5", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Connecté", color = Color(0xFF007AFF), fontSize = 15.sp)
                    }
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
            enabled = !isScanning,
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
                if (!bluetoothPermissions.allPermissionsGranted) "Autoriser le Bluetooth"
                else if (deviceFound) "Continuer"
                else "Commencer le jumelage",
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
fun RadarAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val radius by infiniteTransition.animateFloat(
        initialValue = 50f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)), // More organic curve
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFF007AFF).copy(alpha = alpha),
            radius = radius.dp.toPx(),
            style = Stroke(width = 1.dp.toPx()) // Thinner, elegant stroke
        )
    }
}
