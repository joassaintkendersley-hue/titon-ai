package com.example.data.local

import androidx.room.*
import com.example.data.model.VideoJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoJobDao {
    @Query("SELECT * FROM video_jobs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getJobsForUser(userId: Long): Flow<List<VideoJobEntity>>

    @Query("SELECT * FROM video_jobs WHERE isShowcase = 1 ORDER BY createdAt DESC")
    fun getShowcaseVideos(): Flow<List<VideoJobEntity>>

    @Query("SELECT * FROM video_jobs WHERE id = :jobId")
    fun getJobById(jobId: String): Flow<VideoJobEntity?>

    @Query("SELECT * FROM video_jobs WHERE id = :jobId")
    suspend fun getJobByIdOnce(jobId: String): VideoJobEntity?

    @Query("SELECT * FROM video_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<VideoJobEntity>>

    @Query("SELECT COUNT(*) FROM video_jobs WHERE status = 'COMPLETED'")
    fun getCompletedVideosCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM video_jobs")
    fun getTotalVideosCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: VideoJobEntity)

    @Update
    suspend fun updateJob(job: VideoJobEntity)

    @Query("UPDATE video_jobs SET status = :status, progress = :progress, errorMessage = :error WHERE id = :jobId")
    suspend fun updateJobProgress(jobId: String, status: String, progress: Int, error: String? = null)

    @Query("UPDATE video_jobs SET status = 'COMPLETED', progress = 100, videoUrl = :videoUrl, completedAt = :completedAt WHERE id = :jobId")
    suspend fun completeJob(jobId: String, videoUrl: String, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE video_jobs SET videoUrl = :videoUrl WHERE id = :jobId")
    suspend fun updateVideoUrl(jobId: String, videoUrl: String)

    @Query("DELETE FROM video_jobs WHERE id = :jobId")
    suspend fun deleteJob(jobId: String)
}
