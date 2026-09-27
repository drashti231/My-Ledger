package com.example.data.ai

data class ScannedBillResult(
    val amount: Double,
    val partyName: String,
    val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val category: String = "Other",
    val paymentMethod: String = "Cash",
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val itemsSummary: String = "",
    val confidence: String = "HIGH"
)

data class ParsedTransactionResult(
    val amount: Double,
    val partyName: String,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val paymentMethod: String = "Cash",
    val notes: String = ""
)

data class BusinessInsight(
    val id: String,
    val title: String,
    val description: String,
    val type: InsightType, // ALERT, OPPORTUNITY, SAVINGS, REMINDER
    val actionText: String? = null,
    val actionType: String? = null // "REMINDERS", "EXPENSES", "CUSTOMERS"
)

enum class InsightType {
    ALERT,
    OPPORTUNITY,
    SAVINGS,
    REMINDER
}

data class BusinessSummaryForAi(
    val businessName: String,
    val currency: String,
    val totalRevenue: Double,
    val totalExpense: Double,
    val netProfit: Double,
    val pendingReceivables: Double,
    val pendingPayables: Double,
    val topExpenseCategory: String,
    val topExpenseAmount: Double,
    val overdueCustomersCount: Int
)
