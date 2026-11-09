package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val username: String,
    val passwordHash: String,
    val credits: Int = 50, // 50 Free Welcome Credits
    val role: String = "USER", // "USER" or "ADMIN"
    val avatarSeed: String = "user_default",
    val hasReceivedWelcomeCredits: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
