package com.example.kasa.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.MessageModel
import com.example.kasa.util.AudioPlayerHelper

@Composable
fun AudioBubbleContent(
    message: MessageModel,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playbackState by AudioPlayerHelper.playbackState.collectAsState()

    val isThisAudio = playbackState.currentAudioId == message.id
    val isPlaying = isThisAudio && playbackState.isPlaying

    val currentMs = if (isThisAudio) playbackState.currentPositionMs else 0
    val totalMs = if (isThisAudio && playbackState.totalDurationMs > 0) {
        playbackState.totalDurationMs
    } else {
        message.audioDurationSec * 1000
    }

    val progress = if (totalMs > 0) (currentMs.toFloat() / totalMs).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier.widthIn(min = 210.dp, max = 260.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = if (isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Note vocale",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMe) Color.White else MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play / Pause Circle Button
            Surface(
                shape = CircleShape,
                color = if (isMe) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(38.dp)
                    .clickable {
                        AudioPlayerHelper.play(context, message.id, message.text)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lire",
                        tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Progress Slider & Durations
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                    trackColor = if (isMe) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentSec = currentMs / 1000
                    val totalSec = (totalMs / 1000).coerceAtLeast(1)

                    Text(
                        text = String.format("%02d:%02d", currentSec / 60, currentSec % 60),
                        fontSize = 10.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Text(
                        text = String.format("%02d:%02d", totalSec / 60, totalSec % 60),
                        fontSize = 10.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
