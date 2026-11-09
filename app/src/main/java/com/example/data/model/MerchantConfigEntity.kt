package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_config")
data class MerchantConfigEntity(
    @PrimaryKey
    val id: String = "default_merchant",
    val merchantPhone: String = "+509 34 56 7890",
    val merchantName: String = "TITON AI INC.",
    val ussdCode: String = "*202#",
    val instructions: String = "Dial *202# or open your NatCash app -> Cash In / Transfer to merchant -> Enter exact HTG amount -> Submit SMS transaction reference.",
    val updatedAt: Long = System.currentTimeMillis()
)
