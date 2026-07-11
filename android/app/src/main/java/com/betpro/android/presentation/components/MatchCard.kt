package com.betpro.android.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betpro.android.domain.model.SportEvent

@Composable
fun MatchCard(
    event: SportEvent,
    onAiClick: (SportEvent) -> Unit,
    onOddsClick: (SportEvent, String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aujourd'hui • Football",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF))))
                        .clickable { onAiClick(event) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("✨ IA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Teams
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = event.homeTeam, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text(text = "vs", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp))
                Text(text = event.awayTeam, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Odds 1X2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OddButton(label = "1", value = event.odds1, modifier = Modifier.weight(1f)) { onOddsClick(event, "1", event.odds1) }
                OddButton(label = "X", value = event.oddsX, modifier = Modifier.weight(1f)) { onOddsClick(event, "X", event.oddsX) }
                OddButton(label = "2", value = event.odds2, modifier = Modifier.weight(1f)) { onOddsClick(event, "2", event.odds2) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (event.oddsBttsYes != null) {
                    BadgeItem(text = "BTTS: ${event.oddsBttsYes}")
                }
                if (event.oddsOver25 != null) {
                    BadgeItem(text = "O2.5: ${event.oddsOver25}")
                }
            }
        }
    }
}

@Composable
fun OddButton(label: String, value: Double, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var isSelected by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable {
                isSelected = !isSelected
                onClick()
            }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text(text = value.toString(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun BadgeItem(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
}
