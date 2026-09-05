package com.example.kasa.util

import com.example.kasa.data.model.DebtTransfer
import com.example.kasa.data.model.ExpenseModel
import com.example.kasa.data.model.MemberBalance
import com.example.kasa.data.model.UserModel
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.round

object DebtCalculator {

    data class DebtSummary(
        val totalSpent: Double,
        val fairSharePerPerson: Double,
        val balances: List<MemberBalance>,
        val transfers: List<DebtTransfer>
    )

    fun calculate(
        expenses: List<ExpenseModel>,
        members: List<UserModel>
    ): DebtSummary {
        if (members.isEmpty()) {
            return DebtSummary(0.0, 0.0, emptyList(), emptyList())
        }

        // Exclude settlement expenses from normal expenditure if flagged
        val activeExpenses = expenses.filter { !it.description.startsWith("🤝 [Remboursement]") }
        val totalSpent = activeExpenses.sumOf { it.amount }
        val memberCount = members.size
        val fairShare = if (memberCount > 0) totalSpent / memberCount else 0.0

        // Calculate paid per member
        val balances = members.map { member ->
            val paid = activeExpenses.filter {
                it.userId == member.id || it.userName.equals(member.name, ignoreCase = true)
            }.sumOf { it.amount }

            val net = paid - fairShare
            MemberBalance(
                userId = member.id,
                userName = member.name,
                paidTotal = round2Decimals(paid),
                fairShare = round2Decimals(fairShare),
                netBalance = round2Decimals(net)
            )
        }

        // Debt Simplification (Min Cash Flow algorithm)
        val debtors = ArrayList<Pair<MemberBalance, Double>>()
        val creditors = ArrayList<Pair<MemberBalance, Double>>()

        balances.forEach { b ->
            if (b.netBalance < -0.01) {
                debtors.add(Pair(b, abs(b.netBalance)))
            } else if (b.netBalance > 0.01) {
                creditors.add(Pair(b, b.netBalance))
            }
        }

        // Sort descending by amount
        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val transfers = mutableListOf<DebtTransfer>()

        var debtorIdx = 0
        var creditorIdx = 0

        while (debtorIdx < debtors.size && creditorIdx < creditors.size) {
            val (debtor, debtorAmount) = debtors[debtorIdx]
            val (creditor, creditorAmount) = creditors[creditorIdx]

            val settleAmount = round2Decimals(min(debtorAmount, creditorAmount))

            if (settleAmount > 0.01) {
                transfers.add(
                    DebtTransfer(
                        fromUserId = debtor.userId,
                        fromUserName = debtor.userName,
                        toUserId = creditor.userId,
                        toUserName = creditor.userName,
                        amount = settleAmount
                    )
                )
            }

            val remainingDebtor = debtorAmount - settleAmount
            val remainingCreditor = creditorAmount - settleAmount

            if (remainingDebtor > 0.01) {
                debtors[debtorIdx] = Pair(debtor, remainingDebtor)
            } else {
                debtorIdx++
            }

            if (remainingCreditor > 0.01) {
                creditors[creditorIdx] = Pair(creditor, remainingCreditor)
            } else {
                creditorIdx++
            }
        }

        return DebtSummary(
            totalSpent = round2Decimals(totalSpent),
            fairSharePerPerson = round2Decimals(fairShare),
            balances = balances,
            transfers = transfers
        )
    }

    private fun round2Decimals(value: Double): Double {
        return round(value * 100.0) / 100.0
    }
}
