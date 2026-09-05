package com.example.kasa.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.kasa.data.model.MessageModel
import com.example.kasa.theme.*
import com.example.kasa.util.DateFormatter

// User avatar color generator
private fun getAvatarBrush(name: String): Brush {
    val colors = when ((name.hashCode().coerceAtLeast(0) % 5)) {
        0 -> listOf(Color(0xFF10B981), Color(0xFF059669)) // Emerald
        1 -> listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)) // Blue
        2 -> listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)) // Purple
        3 -> listOf(Color(0xFFF59E0B), Color(0xFFD97706)) // Amber
        else -> listOf(Color(0xFFEC4899), Color(0xFFBE185D)) // Pink
    }
    return Brush.linearGradient(colors)
}

private fun getSenderNameColor(name: String): Color {
    return when ((name.hashCode().coerceAtLeast(0) % 5)) {
        0 -> Color(0xFF059669) // Emerald
        1 -> Color(0xFF2563EB) // Blue
        2 -> Color(0xFF7C3AED) // Purple
        3 -> Color(0xFFD97706) // Amber
        else -> Color(0xFFDB2777) // Pink
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageModel,
    isMe: Boolean,
    modifier: Modifier = Modifier,
    senderIsHome: Boolean = false,
    currentUserName: String = "",
    canDelete: Boolean = false,
    onReply: ((MessageModel) -> Unit)? = null,
    onReact: ((MessageModel, String) -> Unit)? = null,
    onTogglePin: ((MessageModel) -> Unit)? = null,
    onDelete: ((MessageModel) -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    var showActionsMenu by remember { mutableStateOf(false) }
    var showPhotoPreview by remember { mutableStateOf(false) }
    if (message.isSystem) {
        // System activity capsule (Achat, Dépense, Sondage, Info)
        val isPurchase = message.text.contains("🛒") || message.text.contains("acheté")
        val isPoll = message.text.contains("📊") || message.text.contains("sondage")

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when {
                    isPurchase -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    isPoll -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                },
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = when {
                        isPurchase -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        isPoll -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            isPurchase -> "🛒"
                            isPoll -> "📊"
                            else -> "📢"
                        },
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.text.replace("🛒 ", "").replace("📊 ", ""),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            isPurchase -> MaterialTheme.colorScheme.primary
                            isPoll -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DateFormatter.formatTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    } else {
        val isPhoto = message.type == "image" ||
                      message.text.contains("content://") ||
                      message.text.contains("file://") ||
                      message.text.startsWith("📷 [Photo partagée]")

        val photoUri = if (isPhoto) {
            when {
                message.text.contains("content://") -> "content://" + message.text.substringAfter("content://").trim()
                message.text.contains("file://") -> "file://" + message.text.substringAfter("file://").trim()
                else -> message.text
            }
        } else ""

        // Regular user message: LEFT for Others, RIGHT for Me
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(start = if (isMe) 20.dp else 0.dp, end = if (isMe) 0.dp else 20.dp),
                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                // OTHER USER: Avatar on Left
                if (!isMe) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(getAvatarBrush(message.userName)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.userName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Bubble Container
                Column(
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    // Name above incoming bubble with HOME indicator
                    if (!isMe) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 6.dp, bottom = 3.dp)
                        ) {
                            Text(
                                text = message.userName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = getSenderNameColor(message.userName),
                                fontSize = 12.sp
                            )
                            if (senderIsHome) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = KasaSuccess.copy(alpha = 0.16f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, KasaSuccess.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🏠", fontSize = 9.sp)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Maison",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = KasaSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isMe) 18.dp else 2.dp,
                                bottomEnd = if (isMe) 2.dp else 18.dp
                            ),
                            color = if (isMe) Color.Transparent else MaterialTheme.colorScheme.surface,
                            shadowElevation = if (isMe) 3.dp else 1.dp,
                            border = if (isMe) null else androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .shadow(
                                    elevation = if (isMe) 3.dp else 1.dp,
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = if (isMe) 18.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 18.dp
                                    )
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (isPhoto) {
                                            showPhotoPreview = true
                                        }
                                    },
                                    onLongClick = { showActionsMenu = true }
                                )
                        ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = if (isMe) {
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.surface,
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                // Quoted Reply Block if any
                                if (message.isReply) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isMe) Color.Black.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(30.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(if (isMe) Color.White else MaterialTheme.colorScheme.primary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = message.replyToUserName ?: "Membre",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = message.replyToText.orEmpty(),
                                                    fontSize = 11.sp,
                                                    color = if (isMe) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    lineHeight = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isPhoto) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "📷 Photo",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isMe) Color.White else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        coil.compose.AsyncImage(
                                            model = photoUri,
                                            contentDescription = "Photo partagée",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 240.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                } else if (message.isAudio) {
                                    AudioBubbleContent(
                                        message = message,
                                        isMe = isMe
                                    )
                                } else {
                                    Text(
                                        text = message.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 15.sp,
                                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 20.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (message.isPinned) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = "Épinglé",
                                            tint = if (isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = DateFormatter.formatTime(message.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    if (isMe) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "Distribué",
                                            tint = Color.White.copy(alpha = 0.9f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Long-Press Context Menu
                    DropdownMenu(
                        expanded = showActionsMenu,
                        onDismissRequest = { showActionsMenu = false }
                    ) {
                        // Quick Emoji Reactions Row
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("❤️", "👍", "😂", "😮", "😢", "🙏", "🔥").forEach { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 22.sp,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            onReact?.invoke(message, emoji)
                                            showActionsMenu = false
                                        }
                                        .padding(2.dp)
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Répondre") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                onReply?.invoke(message)
                                showActionsMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (message.isPinned) "Désépingler du salon" else "Épingler au salon") },
                            leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                onTogglePin?.invoke(message)
                                showActionsMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Copier le texte") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(message.text))
                                showActionsMenu = false
                            }
                        )
                        if (canDelete) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { Text("Supprimer", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    onDelete?.invoke(message)
                                    showActionsMenu = false
                                }
                            )
                        }
                    }
                }

                // Reactions Pills Row under the bubble
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        message.reactions.forEach { (emoji, userList) ->
                            val hasMyReaction = userList.contains(currentUserName)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (hasMyReaction) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (hasMyReaction) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.clickable { onReact?.invoke(message, emoji) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emoji, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${userList.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasMyReaction) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Photo Fullscreen/Enlarged Preview Dialog
        if (showPhotoPreview && photoUri.isNotEmpty()) {
            Dialog(onDismissRequest = { showPhotoPreview = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Photo partagée",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showPhotoPreview = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fermer"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        coil.compose.AsyncImage(
                            model = photoUri,
                            contentDescription = "Photo agrandie",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 440.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}
