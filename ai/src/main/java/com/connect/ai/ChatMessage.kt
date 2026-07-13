package com.connect.ai

import java.util.UUID

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val isLoading: Boolean = false
) {
    companion object {

    }
}
