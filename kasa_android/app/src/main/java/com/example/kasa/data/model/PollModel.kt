package com.example.kasa.data.model

import java.util.Date

data class PollOption(
    val id: String = "",
    val text: String = "",
    val voterIds: List<String> = emptyList()
) {
    val voteCount: Int get() = voterIds.size
}

data class PollModel(
    val id: String = "",
    val householdId: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val question: String = "",
    val options: List<PollOption> = emptyList(),
    val isClosed: Boolean = false,
    val createdAt: Date = Date()
) {
    val totalVotes: Int get() = options.sumOf { it.voteCount }
    fun hasVoted(userId: String): Boolean = options.any { it.voterIds.contains(userId) }
    fun percentageForOption(option: PollOption): Float {
        val total = totalVotes
        return if (total > 0) option.voteCount.toFloat() / total else 0f
    }
}
