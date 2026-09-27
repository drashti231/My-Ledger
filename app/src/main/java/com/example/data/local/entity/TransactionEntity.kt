package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String, // Food, Shopping, Transport, Salary, Business, Bills, Rent, Marketing, Other
    val dateMillis: Long,
    val notes: String = "",
    val paymentMethod: String = "Cash", // Cash, Bank Transfer, UPI / Card, Cheque, Online
    val referenceId: String? = null,
    val partyName: String? = null
)
