package com.example.kasa.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.PollModel
import com.example.kasa.theme.*

@Composable
fun PollCard(
    poll: PollModel,
    currentUserId: String,
    onVote: (pollId: String, optionId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVotes = poll.totalVotes
    val userVoted = poll.hasVoted(currentUserId)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, KasaSecondary.copy(alpha = 0.25f))
    ) {
        Column {
            // Top Accent Gradient Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(KasaSecondary, KasaPrimary)
                        )
                    )
            )

            Column(modifier = Modifier.padding(18.dp)) {
                // Header: Badge & Votes Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = KasaSecondary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(KasaSecondary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SONDAGE DU FOYER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = KasaSecondaryDark,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HowToVote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalVotes vote(s)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Question
                Text(
                    text = poll.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                if (poll.creatorName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Lancé par ${poll.creatorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Options
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    poll.options.forEach { option ->
                        val isSelected = option.voterIds.contains(currentUserId)
                        val percentage = poll.percentageForOption(option)
                        val animatedPercentage by animateFloatAsState(
                            targetValue = percentage,
                            animationSpec = tween(durationMillis = 400),
                            label = "poll_fill"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) KasaPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .clickable { onVote(poll.id, option.id) }
                        ) {
                            // Animated Gradient Fill
                            if (totalVotes > 0) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .fillMaxWidth(fraction = animatedPercentage.coerceIn(0f, 1f))
                                        .background(
                                            if (isSelected) {
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        KasaPrimary.copy(alpha = 0.25f),
                                                        KasaPrimaryLight.copy(alpha = 0.4f)
                                                    )
                                                )
                                            } else {
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                                    )
                                                )
                                            }
                                        )
                                )
                            }

                            // Content Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) KasaPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = option.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) KasaPrimaryDark else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp
                                    )
                                }

                                Text(
                                    text = if (totalVotes > 0) "${(percentage * 100).toInt()}%" else "${option.voteCount} vote",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) KasaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
