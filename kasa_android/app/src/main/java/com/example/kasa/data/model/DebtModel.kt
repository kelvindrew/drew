package com.example.kasa.data.model

data class MemberBalance(
    val userId: String,
    val userName: String,
    val paidTotal: Double,
    val fairShare: Double,
    val netBalance: Double
) {
    val isCreditor: Boolean get() = netBalance > 0.01
    val isDebtor: Boolean get() = netBalance < -0.01
    val isSettled: Boolean get() = !isCreditor && !isDebtor
}

data class DebtTransfer(
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val amount: Double
)
