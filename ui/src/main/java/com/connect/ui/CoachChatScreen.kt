package com.connect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.connect.ai.ChatMessage
import com.connect.ai.CoachChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachChatScreen(
    viewModel: CoachChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll au dernier message quand la liste change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Apple Style pure black background
    ) {
        // Header
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF2C2C2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Siri Coach", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFF1C1C1E).copy(alpha = 0.9f)
            )
        )

        // Chat History
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        // Input Field (iMessage style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1C1E))
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp)),
                placeholder = { Text("Message", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2C2C2E),
                    unfocusedContainerColor = Color(0xFF2C2C2E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (inputText.isNotBlank()) Color(0xFF34C759) else Color(0xFF2C2C2E)), // Apple Green
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank()
                ) {
                    Text("↑", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(if (isUser) Color(0xFF007AFF) else Color(0xFF2C2C2E)) // Apple Blue vs Dark Gray
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (message.isLoading) {
                Text("...", color = Color.Gray, fontSize = 16.sp, letterSpacing = 2.sp)
            } else {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
