package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerEmail: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val invoiceDateMillis: Long,
    val dueDateMillis: Long,
    val discount: Double = 0.0,
    val taxRate: Double = 0.0,
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val paidAmount: Double = 0.0,
    val status: String = "PENDING", // PAID, PENDING, OVERDUE
    val notes: String = ""
)
