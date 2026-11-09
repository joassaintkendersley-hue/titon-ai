package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_packages")
data class CreditPackageEntity(
    @PrimaryKey
    val id: String, // "STARTER", "PRO", "PREMIUM"
    val name: String,
    val credits: Int,
    val priceHtg: Int, // e.g. 1250, 5000, 12500
    val priceUsd: Double,
    val badge: String? = null, // e.g. "Popular", "Best Value"
    val isPopular: Boolean = false,
    val isActive: Boolean = true
)
