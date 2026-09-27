package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "Alex Morgan",
    val email: String = "alex.morgan@myledger.biz",
    val passwordHash: String = "password123",
    val pin: String = "1234",
    val isPinEnabled: Boolean = true,
    val businessName: String = "NovaTech Solutions",
    val businessPhone: String = "+1 (555) 234-5678",
    val businessEmail: String = "billing@novatech.biz",
    val businessAddress: String = "742 Evergreen Terrace, Suite 300, Silicon City",
    val businessTaxId: String = "TAX-US-8921345-B",
    val currency: String = "$",
    val defaultTaxRate: Double = 10.0,
    val notificationsEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val isLoggedIn: Boolean = true,
    val language: String = "en"
)
