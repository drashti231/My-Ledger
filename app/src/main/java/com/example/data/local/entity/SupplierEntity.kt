package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val totalPurchases: Double = 0.0,
    val paidAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
