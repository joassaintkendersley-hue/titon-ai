package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "natcash_payments")
data class NatcashPaymentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: Long,
    val userEmail: String,
    val packageId: String,
    val packageName: String,
    val creditsAmount: Int,
    val amountHtg: Int,
    val transactionReference: String,
    val senderPhone: String,
    val note: String? = null,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val adminNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)
