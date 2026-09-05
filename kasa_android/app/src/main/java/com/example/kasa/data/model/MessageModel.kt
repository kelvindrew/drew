package com.example.kasa.data.model

import java.util.Date

data class MessageModel(
    val id: String = "",
    val householdId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val type: String = "text", // "text", "system", "poll", "image"
    val replyToMessageId: String? = null,
    val replyToUserName: String? = null,
    val replyToText: String? = null,
    val isPinned: Boolean = false,
    val audioDurationSec: Int = 0,
    val reactions: Map<String, List<String>> = emptyMap(), // emoji -> list of userIds/userNames
    val createdAt: Date = Date()
) {
    val isText: Boolean get() = type == "text"
    val isSystem: Boolean get() = type == "system"
    val isPoll: Boolean get() = type == "poll"
    val isImage: Boolean get() = type == "image"
    val isAudio: Boolean get() = type == "audio"
    val isReply: Boolean get() = !replyToText.isNullOrBlank()
    val totalReactionsCount: Int get() = reactions.values.sumOf { it.size }
}
