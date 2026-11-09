package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provider_configs")
data class ProviderConfigEntity(
    @PrimaryKey
    val id: String, // "seedance", "fal_ai", "replicate", "custom"
    val displayName: String,
    val apiKey: String = "",
    val apiEndpoint: String = "",
    val defaultModel: String = "",
    val cost5s: Int = 10,
    val cost10s: Int = 20,
    val cost15s: Int = 30,
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false
)
