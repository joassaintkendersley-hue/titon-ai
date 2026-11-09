package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "credit_transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: Long,
    val type: String, // "WELCOME_BONUS", "PURCHASE", "USAGE", "REFUND", "ADMIN_ADJUST"
    val amount: Int, // positive for addition, negative for deduction
    val balanceAfter: Int,
    val description: String,
    val referenceId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
