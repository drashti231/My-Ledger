package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String = "",
    val price: Double,
    val stockQuantity: Int = 0,
    val category: String = "General",
    val taxRate: Double = 0.0,
    val description: String = ""
)
