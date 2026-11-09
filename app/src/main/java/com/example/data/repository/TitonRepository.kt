package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.network.VideoApiService
import com.example.data.network.VideoGenerationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class TitonRepository(
    private val database: AppDatabase,
    private val videoApiService: VideoApiService = VideoApiService()
) {
    private val userDao = database.userDao()
    private val videoJobDao = database.videoJobDao()
    private val packageDao = database.packageDao()
    private val natcashPaymentDao = database.natcashPaymentDao()
    private val transactionDao = database.transactionDao()
    private val providerConfigDao = database.providerConfigDao()
    private val merchantConfigDao = database.merchantConfigDao()

    // Current logged-in user state
    private val _currentUserId = MutableStateFlow<Long?>(2L) // Default to demo creator (id=2), admin is id=1
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    fun setCurrentUser(userId: Long?) {
        _currentUserId.value = userId
    }

    // --- User & Auth Operations ---
    fun getCurrentUser(userId: Long): Flow<UserEntity?> = userDao.getUserById(userId)

    suspend fun registerUser(username: String, email: String, password: String): Result<UserEntity> {
        val existing = userDao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }

        // Rule: Every new user receives 50 FREE CREDITS after successful registration
        val newUser = UserEntity(
            email = email.trim().lowercase(),
            username = username.trim(),
            passwordHash = password,
            credits = 50,
            role = "USER",
            avatarSeed = "user_${System.currentTimeMillis() % 1000}",
            hasReceivedWelcomeCredits = true
        )

        val id = userDao.insertUser(newUser)
        val created = newUser.copy(id = id)

        // Maintain transaction history
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = id,
                type = "WELCOME_BONUS",
                amount = 50,
                balanceAfter = 50,
                description = "Welcome Gift: 50 Free Video Generation Credits"
            )
        )

        _currentUserId.value = id
        return Result.success(created)
    }

    suspend fun loginUser(email: String, password: String): Result<UserEntity> {
        val user = userDao.getUserByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("Account not found with this email."))

        if (user.passwordHash != password) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }

        _currentUserId.value = user.id
        return Result.success(user)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        val user = userDao.getUserByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("No account found with this email address."))

        userDao.updateUser(user.copy(passwordHash = newPassword))
        return Result.success(Unit)
    }

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    fun getUserCount(): Flow<Int> = userDao.getUserCount()

    suspend fun updateUserRole(userId: Long, newRole: String) {
        userDao.updateUserRole(userId, newRole)
    }

    suspend fun grantAdminCredits(userId: Long, amount: Int, reason: String) {
        userDao.addCredits(userId, amount)
        val user = userDao.getUserByIdOnce(userId)
        val newBal = user?.credits ?: amount
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "ADMIN_ADJUST",
                amount = amount,
                balanceAfter = newBal,
                description = "Admin Credit Grant: $reason"
            )
        )
    }

    // --- Credit Packages ---
    fun getActivePackages(): Flow<List<CreditPackageEntity>> = packageDao.getActivePackages()
    fun getAllPackages(): Flow<List<CreditPackageEntity>> = packageDao.getAllPackages()

    suspend fun updatePackage(pkg: CreditPackageEntity) {
        packageDao.updatePackage(pkg)
    }

    // --- NatCash Payments (Haiti) ---
    fun getPaymentsForUser(userId: Long): Flow<List<NatcashPaymentEntity>> =
        natcashPaymentDao.getPaymentsForUser(userId)

    fun getAllPayments(): Flow<List<NatcashPaymentEntity>> = natcashPaymentDao.getAllPayments()
    fun getPendingPayments(): Flow<List<NatcashPaymentEntity>> = natcashPaymentDao.getPendingPayments()
    fun getApprovedPayments(): Flow<List<NatcashPaymentEntity>> = natcashPaymentDao.getApprovedPayments()
    fun getRejectedPayments(): Flow<List<NatcashPaymentEntity>> = natcashPaymentDao.getRejectedPayments()
    fun getPendingPaymentsCount(): Flow<Int> = natcashPaymentDao.getPendingPaymentsCount()
    fun getApprovedPaymentsCount(): Flow<Int> = natcashPaymentDao.getApprovedPaymentsCount()
    fun getRejectedPaymentsCount(): Flow<Int> = natcashPaymentDao.getRejectedPaymentsCount()
    fun getTotalCreditsSold(): Flow<Int?> = natcashPaymentDao.getTotalCreditsSold()
    fun getTotalRevenueHtg(): Flow<Int?> = natcashPaymentDao.getTotalRevenueHtg()

    // Configurable Merchant Credentials
    fun getMerchantConfig(): Flow<MerchantConfigEntity?> = merchantConfigDao.getMerchantConfig()
    suspend fun getMerchantConfigOnce(): MerchantConfigEntity =
        merchantConfigDao.getMerchantConfigOnce() ?: MerchantConfigEntity()
    suspend fun updateMerchantConfig(config: MerchantConfigEntity) =
        merchantConfigDao.insertOrUpdate(config)

    suspend fun submitNatcashPayment(
        userId: Long,
        userEmail: String,
        pkg: CreditPackageEntity,
        transactionReference: String,
        senderPhone: String,
        userNote: String?
    ): Result<NatcashPaymentEntity> {
        if (transactionReference.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your NatCash Transaction Reference."))
        }
        if (senderPhone.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your NatCash sender phone number."))
        }

        val payment = NatcashPaymentEntity(
            userId = userId,
            userEmail = userEmail,
            packageId = pkg.id,
            packageName = pkg.name,
            creditsAmount = pkg.credits,
            amountHtg = pkg.priceHtg,
            transactionReference = transactionReference.trim(),
            senderPhone = senderPhone.trim(),
            note = userNote?.trim(),
            status = "PENDING"
        )

        natcashPaymentDao.insertPayment(payment)
        return Result.success(payment)
    }

    suspend fun approveNatcashPayment(paymentId: String, adminNotes: String?): Result<Unit> {
        val payment = natcashPaymentDao.getPaymentById(paymentId)
            ?: return Result.failure(IllegalArgumentException("Payment not found"))

        if (payment.status == "APPROVED") {
            return Result.failure(IllegalStateException("Payment has already been approved."))
        }

        // 1. Mark payment approved
        natcashPaymentDao.updatePaymentStatus(paymentId, "APPROVED", adminNotes)

        // 2. Add credits to user account
        userDao.addCredits(payment.userId, payment.creditsAmount)

        // 3. Record transaction
        val user = userDao.getUserByIdOnce(payment.userId)
        val balanceAfter = user?.credits ?: payment.creditsAmount
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = payment.userId,
                type = "PURCHASE",
                amount = payment.creditsAmount,
                balanceAfter = balanceAfter,
                description = "NatCash Purchase: ${payment.packageName} (${payment.amountHtg} HTG)",
                referenceId = payment.transactionReference
            )
        )

        return Result.success(Unit)
    }

    suspend fun rejectNatcashPayment(paymentId: String, reason: String): Result<Unit> {
        val payment = natcashPaymentDao.getPaymentById(paymentId)
            ?: return Result.failure(IllegalArgumentException("Payment not found"))

        natcashPaymentDao.updatePaymentStatus(paymentId, "REJECTED", reason)
        return Result.success(Unit)
    }

    // --- Transactions ---
    fun getTransactionsForUser(userId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForUser(userId)

    fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    // --- AI Video Providers ---
    fun getAllProviders(): Flow<List<ProviderConfigEntity>> = providerConfigDao.getAllProviders()

    suspend fun updateProviderConfig(config: ProviderConfigEntity) {
        providerConfigDao.updateProvider(config)
    }

    suspend fun testApiConnection(provider: ProviderConfigEntity, apiKey: String): Result<String> {
        return videoApiService.testConnection(provider, apiKey)
    }

    /**
     * Resolves the effective API key by securely querying:
     * 1. Local database provider configuration
     * 2. Backend system environment variable (e.g. FAL_KEY, REPLICATE_API_TOKEN)
     * 3. BuildConfig injected via Secrets Gradle plugin from .env
     *
     * Sanitizes keys and excludes any default/placeholder tokens.
     */
    fun getEffectiveApiKey(provider: ProviderConfigEntity): String {
        // Priority 1: User explicitly configured in app DB (and not a placeholder)
        val dbKey = VideoApiService.sanitizeApiKey(provider.apiKey)
        if (!VideoApiService.isPlaceholderKey(dbKey)) {
            return dbKey
        }

        // Priority 2: System backend environment variables
        val envKey = when (provider.id.lowercase()) {
            "fal_ai" -> System.getenv("FAL_KEY")
            "replicate" -> System.getenv("REPLICATE_API_TOKEN")
            "custom" -> System.getenv("TITON_BACKEND_URL")
            else -> null
        }
        val cleanEnvKey = VideoApiService.sanitizeApiKey(envKey)
        if (!VideoApiService.isPlaceholderKey(cleanEnvKey)) {
            return cleanEnvKey
        }

        // Priority 3: BuildConfig injected from .env
        val buildConfigKey = try {
            when (provider.id.lowercase()) {
                "fal_ai" -> BuildConfig.FAL_KEY
                "replicate" -> BuildConfig.REPLICATE_API_TOKEN
                "custom" -> BuildConfig.TITON_BACKEND_URL
                else -> ""
            }
        } catch (_: Throwable) {
            ""
        }
        val cleanBuildConfigKey = VideoApiService.sanitizeApiKey(buildConfigKey)
        if (!VideoApiService.isPlaceholderKey(cleanBuildConfigKey)) {
            return cleanBuildConfigKey
        }

        return ""
    }

    // --- Video Generation & History ---
    fun getVideosForUser(userId: Long): Flow<List<VideoJobEntity>> =
        videoJobDao.getJobsForUser(userId)

    fun getShowcaseVideos(): Flow<List<VideoJobEntity>> = videoJobDao.getShowcaseVideos()
    fun getAllVideos(): Flow<List<VideoJobEntity>> = videoJobDao.getAllJobs()
    fun getCompletedVideosCount(): Flow<Int> = videoJobDao.getCompletedVideosCount()
    fun getTotalVideosCount(): Flow<Int> = videoJobDao.getTotalVideosCount()

    /**
     * Core AI Video Generation logic:
     * 1. Connect a real video generation provider (Fal.ai, Replicate, or backend proxy).
     * 2. Verify authentication before submitting requests.
     * 3. Deduct credits ONLY after successful generation.
     * 4. Refunds or avoids deduction if generation fails.
     * 5. If no API provider is configured, clearly surfaces requirement instead of fake results.
     */
    suspend fun requestVideoGeneration(
        userId: Long,
        prompt: String,
        negativePrompt: String,
        imageUrl: String?,
        aspectRatio: String,
        duration: Int,
        quality: String,
        providerId: String
    ): Result<VideoJobEntity> {
        if (prompt.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a video prompt description."))
        }

        val user = userDao.getUserByIdOnce(userId)
            ?: return Result.failure(IllegalStateException("User not found."))

        val provider = providerConfigDao.getProviderById(providerId)
            ?: return Result.failure(IllegalStateException("Selected AI provider not found."))

        // Calculate credit cost based on duration
        val creditCost = when (duration) {
            5 -> provider.cost5s
            10 -> provider.cost10s
            15 -> provider.cost15s
            else -> 10
        }

        // Prevent negative credit balance
        if (user.credits < creditCost) {
            return Result.failure(
                IllegalStateException(
                    "Insufficient credits. This video requires $creditCost credits, but your balance is ${user.credits} credits. Please buy credits to continue."
                )
            )
        }

        val resolvedApiKey = getEffectiveApiKey(provider)

        // Strict Requirement: Validate API credentials for real providers before submitting
        val requiresKey = provider.id.lowercase() in listOf("fal_ai", "replicate")
        if ((requiresKey && VideoApiService.isPlaceholderKey(resolvedApiKey)) ||
            (VideoApiService.isPlaceholderKey(resolvedApiKey) && provider.apiEndpoint.isBlank())) {

            val configMsg = when (provider.id.lowercase()) {
                "fal_ai" -> "Fal.ai Authentication Required: FAL_KEY is not configured. Please add your FAL_KEY securely in the AI Studio Secrets panel or configure it in Studio Settings to generate AI videos."
                "replicate" -> "Replicate Token Required: REPLICATE_API_TOKEN is not configured. Please add your API token in AI Studio Secrets or Studio Settings."
                else -> "API Configuration Required: No API key or backend proxy configured for ${provider.displayName}."
            }

            val failedJob = VideoJobEntity(
                userId = userId,
                prompt = prompt,
                negativePrompt = negativePrompt,
                imageUrl = imageUrl,
                aspectRatio = aspectRatio,
                duration = duration,
                quality = quality,
                status = "FAILED",
                progress = 0,
                provider = provider.displayName,
                creditsCost = creditCost,
                errorMessage = configMsg
            )
            videoJobDao.insertJob(failedJob)

            // NO credits are deducted when configuration is missing!
            return Result.failure(IllegalStateException(configMsg))
        }

        // Create Pending Job in Database
        val job = VideoJobEntity(
            userId = userId,
            prompt = prompt,
            negativePrompt = negativePrompt,
            imageUrl = imageUrl,
            aspectRatio = aspectRatio,
            duration = duration,
            quality = quality,
            status = "PROCESSING",
            progress = 10,
            provider = provider.displayName,
            creditsCost = creditCost
        )
        videoJobDao.insertJob(job)

        // Execute real video generation with progress tracking
        val generationResult = videoApiService.generateVideo(
            provider = provider,
            resolvedApiKey = resolvedApiKey,
            job = job,
            onProgress = { percent, statusMsg ->
                videoJobDao.updateJobProgress(job.id, "PROCESSING", percent, statusMsg)
            }
        )

        when (generationResult) {
            is VideoGenerationResult.Success -> {
                // Deduct credits ONLY after successful generation!
                val deducted = userDao.deductCredits(userId, creditCost)
                if (deducted > 0) {
                    val updatedUser = userDao.getUserByIdOnce(userId)
                    transactionDao.insertTransaction(
                        TransactionEntity(
                            userId = userId,
                            type = "USAGE",
                            amount = -creditCost,
                            balanceAfter = updatedUser?.credits ?: 0,
                            description = "AI Video Generation: $duration-sec ($quality) via ${provider.displayName}"
                        )
                    )
                }

                // Save generated MP4 video to My Videos
                videoJobDao.completeJob(job.id, generationResult.videoUrl)
                val completed = videoJobDao.getJobByIdOnce(job.id) ?: job.copy(
                    status = "COMPLETED",
                    progress = 100,
                    videoUrl = generationResult.videoUrl
                )
                return Result.success(completed)
            }
            is VideoGenerationResult.Failure -> {
                // Credits are NOT consumed if generation fails
                videoJobDao.updateJobProgress(
                    job.id,
                    "FAILED",
                    0,
                    generationResult.errorMessage
                )

                return Result.failure(IllegalStateException(generationResult.errorMessage))
            }
            else -> {
                return Result.failure(IllegalStateException("Video generation failed to return completed status."))
            }
        }
    }

    suspend fun downloadVideo(context: Context, videoUrl: String, prompt: String): Result<File> {
        val safeName = "titon_" + prompt.take(15).replace("[^a-zA-Z0-9]".toRegex(), "_") + "_" + System.currentTimeMillis()
        return videoApiService.downloadVideoToDevice(context, videoUrl, safeName)
    }

    suspend fun deleteVideo(jobId: String) {
        videoJobDao.deleteJob(jobId)
    }
}
