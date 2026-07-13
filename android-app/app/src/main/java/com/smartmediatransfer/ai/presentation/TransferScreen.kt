package com.smartmediatransfer.ai.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.smartmediatransfer.ai.presentation.theme.ElectricBlue
import com.smartmediatransfer.ai.presentation.theme.Turquoise

@Composable
fun TransferScreen() {
    // val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.transfer_particles))
    // val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                Icon(
                    Icons.Default.Warning, // fallback icon
                    contentDescription = "Phone",
                    tint = Color.Gray,
                    modifier = Modifier.size(80.dp)
                )

                Box(modifier = Modifier.width(150.dp).height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Turquoise)
                }

                Icon(
                    Icons.Default.Send, // fallback icon
                    contentDescription = "PC",
                    tint = Color.White,
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Transfert en cours...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Inception_1080p.mkv",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            LinearProgressIndicator(
                progress = 0.65f,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(8.dp),
                color = ElectricBlue,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("65%", color = ElectricBlue, fontWeight = FontWeight.Bold)
                Text("120 MB/s", color = Color.Gray)
            }
        }
    }
}
