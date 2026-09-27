package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val amount: Double,
    val paymentDateMillis: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Bank Transfer",
    val notes: String = ""
)
