package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "video_jobs")
data class VideoJobEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: Long,
    val prompt: String,
    val negativePrompt: String = "",
    val imageUrl: String? = null, // for image-to-video
    val aspectRatio: String = "16:9", // "16:9", "9:16", "1:1"
    val duration: Int = 5, // 5, 10, 15
    val quality: String = "High Quality", // "Standard", "High Quality", "Ultra HD"
    val status: String = "PENDING", // "PENDING", "PROCESSING", "COMPLETED", "FAILED"
    val progress: Int = 0, // 0 - 100
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val provider: String = "Seedance AI",
    val creditsCost: Int = 10,
    val errorMessage: String? = null,
    val isShowcase: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
