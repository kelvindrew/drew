package com.example.kasa.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.kasa.data.model.MessageModel
import com.example.kasa.data.model.PollModel
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.*
import com.example.kasa.ui.components.MessageBubble
import com.example.kasa.ui.components.PollCard
import com.example.kasa.ui.components.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun ChatWallpaperBackground(
    wallpaper: ChatWallpaper,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val bg = MaterialTheme.colorScheme.background

    when (wallpaper) {
        ChatWallpaper.MINIMAL -> {
            Box(modifier = modifier.fillMaxSize().background(bg))
        }
        ChatWallpaper.AURORA -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                bg,
                                primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
                                bg
                            )
                        )
                    )
            )
        }
        ChatWallpaper.STARRY -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(primary.copy(alpha = 0.12f), bg),
                            center = Offset(200f, 300f),
                            radius = 900f
                        )
                    )
            )
        }
        ChatWallpaper.GEOMETRIC -> {
            Box(modifier = modifier.fillMaxSize().background(bg)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 40.dp.toPx()
                    val dotRadius = 1.5.dp.toPx()
                    val dotColor = primary.copy(alpha = 0.12f)
                    var x = step / 2
                    while (x < size.width) {
                        var y = step / 2
                        while (y < size.height) {
                            drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                            y += step
                        }
                        x += step
                    }
                }
            }
        }
        ChatWallpaper.DOODLES -> {
            Box(modifier = modifier.fillMaxSize().background(bg)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 60.dp.toPx()
                    val circleColor = primary.copy(alpha = 0.07f)
                    var x = step / 2
                    while (x < size.width) {
                        var y = step / 2
                        while (y < size.height) {
                            drawCircle(color = circleColor, radius = 6.dp.toPx(), center = Offset(x, y))
                            y += step
                        }
                        x += step
                    }
                }
            }
        }
    }
}

sealed class ChatFeedItem(val id: String, val timestamp: Long) {
    data class Message(val msg: MessageModel) : ChatFeedItem(msg.id, msg.createdAt.time)
    data class Poll(val pollData: PollModel) : ChatFeedItem(pollData.id, pollData.createdAt.time)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier
) {
    val household by KasaRepository.currentHousehold.collectAsState()
    val currentUser by KasaRepository.currentUser.collectAsState()
    val messages by KasaRepository.messages.collectAsState()
    val polls by KasaRepository.polls.collectAsState()
    val members by KasaRepository.members.collectAsState()
    val chatWallpaper by ThemeManager.chatWallpaper.collectAsState()
    val mealNotes by KasaRepository.mealNotes.collectAsState()
    val postIts by KasaRepository.postIts.collectAsState()
    val houseEssentials by KasaRepository.houseEssentials.collectAsState()
    val activeGuestEvent by KasaRepository.activeGuestEvent.collectAsState()
    val scheduledReminders by KasaRepository.scheduledReminders.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<MessageModel?>(null) }
    var showCreatePollDialog by remember { mutableStateOf(false) }
    var showMealNotesDialog by remember { mutableStateOf(false) }
    var showCorkboardDialog by remember { mutableStateOf(false) }
    var showWifiQrDialog by remember { mutableStateOf(false) }
    var showGuestPartyDialog by remember { mutableStateOf(false) }
    var showMediaGalleryDialog by remember { mutableStateOf(false) }
    var showScheduledReminderDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        KasaRepository.checkDueReminders()
        while (isActive) {
            kotlinx.coroutines.delay(30000)
            KasaRepository.checkDueReminders()
        }
    }

    val rawFeedItems = remember(messages, polls) {
        val list = ArrayList<ChatFeedItem>(messages.size + polls.size)
        messages.forEach { list.add(ChatFeedItem.Message(it)) }
        polls.forEach { list.add(ChatFeedItem.Poll(it)) }
        list.sortedBy { it.timestamp }
    }

    val feedItems = remember(rawFeedItems, searchQuery) {
        if (searchQuery.isBlank()) {
            rawFeedItems
        } else {
            rawFeedItems.filter { item ->
                when (item) {
                    is ChatFeedItem.Message -> item.msg.text.contains(searchQuery, ignoreCase = true) ||
                            item.msg.userName.contains(searchQuery, ignoreCase = true)
                    is ChatFeedItem.Poll -> item.pollData.question.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    val pinnedMessage = remember(messages) {
        messages.findLast { it.isPinned }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            KasaRepository.sendMessage(it.toString(), "image")
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingElapsedSec by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingElapsedSec = 0
            while (isActive && isRecordingAudio) {
                kotlinx.coroutines.delay(500)
                recordingElapsedSec = com.example.kasa.util.AudioRecorderHelper.getElapsedSeconds()
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val started = com.example.kasa.util.AudioRecorderHelper.startRecording(context)
            isRecordingAudio = started
        }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val myUserId = currentUser?.id ?: ""

    // Auto-scroll on new message or poll
    LaunchedEffect(feedItems.size) {
        if (feedItems.isNotEmpty()) {
            listState.animateScrollToItem(feedItems.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Info & Quick Actions Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Rechercher un message...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    isSearchActive = false
                                    searchQuery = ""
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Fermer la recherche",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Salon du Foyer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                            Text(
                                text = "${household?.name ?: "Mon Foyer"} • ${if (members.isNotEmpty()) "${members.size} membre(s)" else "En ligne"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Tableau de Liège Virtuel (Post-it & Règles)
                            Box {
                                IconButton(
                                    onClick = { showCorkboardDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("📌", fontSize = 17.sp)
                                }
                                if (postIts.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-2).dp, y = 2.dp)
                                    ) {
                                        Text("${postIts.size}", fontSize = 9.sp)
                                    }
                                }
                            }

                            // Galerie photos du chat
                            IconButton(
                                onClick = { showMediaGalleryDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("🖼️", fontSize = 17.sp)
                            }

                            // Rappels Programmés
                            val activeRemindersCount = scheduledReminders.count { !it.isSent }
                            Box {
                                IconButton(
                                    onClick = { showScheduledReminderDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("⏰", fontSize = 17.sp)
                                }
                                if (activeRemindersCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-2).dp, y = 2.dp)
                                    ) {
                                        Text("$activeRemindersCount", fontSize = 9.sp)
                                    }
                                }
                            }

                            // Bloc-notes des Repas du Foyer
                            Box {
                                IconButton(
                                    onClick = { showMealNotesDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("🍽️", fontSize = 17.sp)
                                }
                                if (mealNotes.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-2).dp, y = 2.dp)
                                    ) {
                                        Text("${mealNotes.size}", fontSize = 9.sp)
                                    }
                                }
                            }

                            IconButton(
                                onClick = { isSearchActive = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Rechercher",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Guest Party Banner
        if (activeGuestEvent != null && !isSearchActive) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGuestPartyDialog = true },
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎉", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Soirée Actif • ${activeGuestEvent?.hostUserName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "~${activeGuestEvent?.guestCount} invité(s)${if (!activeGuestEvent?.description.isNullOrBlank()) " • \"${activeGuestEvent?.description}\"" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp
                        )
                    }
                    TextButton(
                        onClick = { showGuestPartyDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Gérer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Pinned Message Quick Access Banner
        if (pinnedMessage != null && !isSearchActive) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val targetIndex = feedItems.indexOfFirst { it.id == pinnedMessage.id }
                        if (targetIndex >= 0) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(targetIndex)
                            }
                        }
                    },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Message épinglé • ${pinnedMessage.userName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = pinnedMessage.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = { KasaRepository.togglePinMessage(pinnedMessage.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Désépingler",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Messages & Polls Stream with Wallpaper
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            ChatWallpaperBackground(wallpaper = chatWallpaper)

            if (feedItems.isEmpty()) {
                // Empty state card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isSearchActive) "🔍" else "💬", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isSearchActive) "Aucun résultat trouvé" else "Bienvenue dans le salon !",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSearchActive) "Aucun message ou sondage ne correspond à \"$searchQuery\"."
                        else "Discutez avec les membres du foyer, lancez des sondages ou partagez les achats en temps réel.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    if (!isSearchActive) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { showCreatePollDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Poll, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lancer un sondage", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
                ) {
                    items(feedItems, key = { it.id }) { item ->
                        when (item) {
                            is ChatFeedItem.Message -> {
                                val message = item.msg
                                val isMe = (myUserId.isNotEmpty() && message.userId == myUserId) ||
                                           (currentUser?.name?.isNotEmpty() == true && message.userName.equals(currentUser?.name, ignoreCase = true))
                                val senderMember = members.find { it.id == message.userId || it.name.equals(message.userName, ignoreCase = true) }
                                val senderIsHome = if (isMe) (currentUser?.isHome == true) else (senderMember?.isHome == true)
                                val canDelete = isMe || currentUser?.role == "admin"

                                MessageBubble(
                                    message = message,
                                    isMe = isMe,
                                    senderIsHome = senderIsHome,
                                    currentUserName = currentUser?.name.orEmpty(),
                                    canDelete = canDelete,
                                    onReply = { replyingToMessage = it },
                                    onReact = { msg, emoji -> KasaRepository.reactToMessage(msg.id, emoji) },
                                    onTogglePin = { msg -> KasaRepository.togglePinMessage(msg.id) },
                                    onDelete = { msg -> KasaRepository.deleteMessage(msg.id) }
                                )
                            }
                            is ChatFeedItem.Poll -> {
                                PollCard(
                                    poll = item.pollData,
                                    currentUserId = myUserId,
                                    onVote = { pollId, optId ->
                                        KasaRepository.votePoll(pollId, optId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input Dock with Quoted Reply Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Quoted Message Banner Docked Above Input
                if (replyingToMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Reply,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Réponse à ${replyingToMessage?.userName ?: "Membre"}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = replyingToMessage?.text.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = { replyingToMessage = null },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Annuler la réponse",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo Attach Button
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Partager une photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Quick Poll Button
                    IconButton(
                        onClick = { showCreatePollDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            Icons.Default.Poll,
                            contentDescription = "Créer un sondage",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isRecordingAudio) {
                        // Cancel audio recording
                        IconButton(
                            onClick = {
                                com.example.kasa.util.AudioRecorderHelper.cancelRecording()
                                isRecordingAudio = false
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Annuler l'enregistrement",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Pulsing timer bubble
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Note vocale... ${String.format("%02d:%02d", recordingElapsedSec / 60, recordingElapsedSec % 60)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Audio Button
                        IconButton(
                            onClick = {
                                val result = com.example.kasa.util.AudioRecorderHelper.stopRecording()
                                isRecordingAudio = false
                                if (result != null) {
                                    val (file, duration) = result
                                    KasaRepository.sendMessage(
                                        text = file.absolutePath,
                                        type = "audio",
                                        audioDurationSec = duration
                                    )
                                    coroutineScope.launch {
                                        if (feedItems.isNotEmpty()) {
                                            listState.animateScrollToItem(feedItems.size)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                                    )
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Envoyer l'audio",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        // Input Field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = if (replyingToMessage != null) "Votre réponse..." else "Écrire au foyer...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(26.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Button or Microphone Button
                        val canSend = inputText.isNotBlank()
                        if (canSend) {
                            IconButton(
                                onClick = {
                                    val rep = replyingToMessage
                                    KasaRepository.sendMessage(
                                        text = inputText.trim(),
                                        type = "text",
                                        replyToMessageId = rep?.id,
                                        replyToUserName = rep?.userName,
                                        replyToText = rep?.text
                                    )
                                    inputText = ""
                                    replyingToMessage = null
                                    coroutineScope.launch {
                                        if (feedItems.isNotEmpty()) {
                                            listState.animateScrollToItem(feedItems.size)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Envoyer",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            // Microphone Button
                            IconButton(
                                onClick = {
                                    val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.RECORD_AUDIO
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        val started = com.example.kasa.util.AudioRecorderHelper.startRecording(context)
                                        isRecordingAudio = started
                                    } else {
                                        audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .shadow(2.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Enregistrer un message vocal",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreatePollDialog) {
        CreatePollDialog(
            onDismiss = { showCreatePollDialog = false },
            onConfirm = { question, options ->
                KasaRepository.createPoll(question, options)
                showCreatePollDialog = false
            }
        )
    }

    if (showMealNotesDialog) {
        com.example.kasa.ui.components.MealNotesDialog(
            notes = mealNotes,
            currentUser = currentUser,
            isAdmin = currentUser?.isAdmin == true,
            onDismiss = { showMealNotesDialog = false }
        )
    }

    if (showCorkboardDialog) {
        com.example.kasa.ui.components.CorkboardDialog(
            postIts = postIts,
            houseEssentials = houseEssentials,
            currentUser = currentUser,
            isAdmin = currentUser?.isAdmin == true,
            onDismiss = { showCorkboardDialog = false },
            onOpenWifiQr = {
                showCorkboardDialog = false
                showWifiQrDialog = true
            }
        )
    }

    if (showWifiQrDialog) {
        com.example.kasa.ui.components.WifiQrDialog(
            wifiSsid = houseEssentials.wifiSsid,
            wifiPassword = houseEssentials.wifiPassword,
            onDismiss = { showWifiQrDialog = false }
        )
    }

    if (showGuestPartyDialog) {
        com.example.kasa.ui.components.GuestPartyDialog(
            activeEvent = activeGuestEvent,
            currentUser = currentUser,
            onDismiss = { showGuestPartyDialog = false }
        )
    }

    if (showMediaGalleryDialog) {
        com.example.kasa.ui.components.ChatMediaGalleryDialog(
            messages = messages,
            onDismiss = { showMediaGalleryDialog = false }
        )
    }

    if (showScheduledReminderDialog) {
        com.example.kasa.ui.components.ScheduledReminderDialog(
            reminders = scheduledReminders,
            currentUser = currentUser,
            isAdmin = currentUser?.isAdmin == true,
            onDismiss = { showScheduledReminderDialog = false }
        )
    }
}

@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onConfirm: (question: String, options: List<String>) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf("", "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📊", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nouveau sondage", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question du sondage") },
                    placeholder = { Text("Ex: Menu ce soir ? Répartition corvées ?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Options de réponse", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                options.forEachIndexed { index, optionText ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = optionText,
                            onValueChange = { newText ->
                                val updated = options.toMutableList()
                                updated[index] = newText
                                options = updated
                            },
                            label = { Text("Option ${index + 1}") },
                            placeholder = { Text("Ex: Option ${index + 1}") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (options.size > 2) {
                            IconButton(
                                onClick = {
                                    val updated = options.toMutableList()
                                    updated.removeAt(index)
                                    options = updated
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = KasaError)
                            }
                        }
                    }
                }

                if (options.size < 5) {
                    TextButton(
                        onClick = { options = options + "" },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter une option", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            val validOptions = options.filter { it.trim().isNotBlank() }
            val canSubmit = question.trim().isNotBlank() && validOptions.size >= 2
            Button(
                onClick = {
                    if (canSubmit) {
                        onConfirm(question.trim(), validOptions.map { it.trim() })
                    }
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Publier le sondage", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
