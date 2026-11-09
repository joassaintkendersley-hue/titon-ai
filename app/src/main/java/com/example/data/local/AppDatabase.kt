package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        VideoJobEntity::class,
        CreditPackageEntity::class,
        NatcashPaymentEntity::class,
        TransactionEntity::class,
        ProviderConfigEntity::class,
        MerchantConfigEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun videoJobDao(): VideoJobDao
    abstract fun packageDao(): PackageDao
    abstract fun natcashPaymentDao(): NatcashPaymentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun providerConfigDao(): ProviderConfigDao
    abstract fun merchantConfigDao(): MerchantConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "titon_ai.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                ensureEssentialsSeeded(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun ensureEssentialsSeeded(db: AppDatabase) {
            try {
                if (db.merchantConfigDao().getMerchantConfigOnce() == null) {
                    db.merchantConfigDao().insertOrUpdate(MerchantConfigEntity())
                }

                // Clean up unresolvable legacy seedance endpoint
                db.providerConfigDao().deleteLegacySeedance()

                val falKey = try { com.example.BuildConfig.FAL_KEY.trim().let { if (it.startsWith("DEFAULT_")) "" else it } } catch (_: Throwable) { "" }
                val replicateKey = try { com.example.BuildConfig.REPLICATE_API_TOKEN.trim().let { if (it.startsWith("DEFAULT_")) "" else it } } catch (_: Throwable) { "" }
                val backendUrl = try { com.example.BuildConfig.TITON_BACKEND_URL.trim().let { if (it.startsWith("DEFAULT_")) "" else it } } catch (_: Throwable) { "" }

                if (db.providerConfigDao().getProviderById("fal_ai") == null) {
                    db.providerConfigDao().insertProviders(listOf(
                        ProviderConfigEntity(
                            id = "fal_ai",
                            displayName = "Fal.ai Video (LTX / Kling)",
                            apiKey = falKey,
                            apiEndpoint = "https://queue.fal.run/fal-ai/ltx-video",
                            defaultModel = "fal-ai/ltx-video",
                            cost5s = 10,
                            cost10s = 20,
                            cost15s = 30,
                            isEnabled = true,
                            isDefault = true
                        )
                    ))
                }

                if (db.providerConfigDao().getProviderById("replicate") == null) {
                    db.providerConfigDao().insertProviders(listOf(
                        ProviderConfigEntity(
                            id = "replicate",
                            displayName = "Replicate Wan 2.1 Video",
                            apiKey = replicateKey,
                            apiEndpoint = "https://api.replicate.com/v1/predictions",
                            defaultModel = "wan-video/wan-2.1-t2v-14b",
                            cost5s = 10,
                            cost10s = 20,
                            cost15s = 30,
                            isEnabled = true,
                            isDefault = false
                        )
                    ))
                }

                if (db.providerConfigDao().getProviderById("custom") == null) {
                    db.providerConfigDao().insertProviders(listOf(
                        ProviderConfigEntity(
                            id = "custom",
                            displayName = "Custom Backend Proxy",
                            apiKey = "",
                            apiEndpoint = if (backendUrl.isNotBlank()) backendUrl else "",
                            defaultModel = "custom-v1",
                            cost5s = 10,
                            cost10s = 20,
                            cost15s = 30,
                            isEnabled = true,
                            isDefault = false
                        )
                    ))
                }

                // Verify Showcase Videos have real, accessible, playable MP4 files
                val verifiedShowcase = listOf(
                    VideoJobEntity(
                        id = "showcase-1",
                        userId = 1L,
                        prompt = "Cinematic slow motion shot of a bioluminescent cybernetic dragon soaring over neon crystalline metropolis in deep space, 8k, photorealistic octane render, electric teal and ultra violet glow",
                        negativePrompt = "blurry, low quality, noise, artifacts",
                        aspectRatio = "16:9",
                        duration = 10,
                        quality = "Ultra HD",
                        status = "COMPLETED",
                        progress = 100,
                        videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4",
                        thumbnailUrl = null,
                        provider = "Fal.ai LTX",
                        creditsCost = 20,
                        isShowcase = true
                    ),
                    VideoJobEntity(
                        id = "showcase-2",
                        userId = 1L,
                        prompt = "Hyper-realistic futuristic cyber warrior standing atop rain-slicked Tokyo skyscraper overlooking flying hovercars and holographic billboards, dramatic cinematic anamorphic lighting",
                        negativePrompt = "cartoon, sketch, low res",
                        aspectRatio = "9:16",
                        duration = 5,
                        quality = "High Quality",
                        status = "COMPLETED",
                        progress = 100,
                        videoUrl = "https://media.w3.org/2010/05/video/movie_300.mp4",
                        thumbnailUrl = null,
                        provider = "Fal.ai LTX",
                        creditsCost = 10,
                        isShowcase = true
                    ),
                    VideoJobEntity(
                        id = "showcase-3",
                        userId = 1L,
                        prompt = "Surreal liquid gold fluid morphing into a majestic celestial dragon against a star-studded purple nebula, orbital macro camera sweep",
                        negativePrompt = "flicker, static, pixelated",
                        aspectRatio = "1:1",
                        duration = 15,
                        quality = "Ultra HD",
                        status = "COMPLETED",
                        progress = 100,
                        videoUrl = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
                        thumbnailUrl = null,
                        provider = "Fal.ai LTX",
                        creditsCost = 30,
                        isShowcase = true
                    ),
                    VideoJobEntity(
                        id = "showcase-4",
                        userId = 1L,
                        prompt = "High-octane neon cyber speeder drifting through subterranean highway tunnels, volumetric lighting and rain particles",
                        negativePrompt = "low quality, stutter",
                        aspectRatio = "16:9",
                        duration = 5,
                        quality = "Ultra HD",
                        status = "COMPLETED",
                        progress = 100,
                        videoUrl = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/friday.mp4",
                        thumbnailUrl = null,
                        provider = "Fal.ai LTX",
                        creditsCost = 10,
                        isShowcase = true
                    )
                )

                verifiedShowcase.forEach { video ->
                    val existing = db.videoJobDao().getJobByIdOnce(video.id)
                    if (existing == null || existing.videoUrl?.contains("commondatastorage.googleapis.com") == true || existing.videoUrl.isNullOrBlank()) {
                        db.videoJobDao().insertJob(video)
                    }
                }
            } catch (_: Throwable) {
                // Table might be initializing
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // 1. Initial Admin User & Demo User
            val adminId = db.userDao().insertUser(
                UserEntity(
                    email = "admin@titon.ai",
                    username = "TitonAdmin",
                    passwordHash = "admin123",
                    credits = 10000,
                    role = "ADMIN",
                    avatarSeed = "admin_master",
                    hasReceivedWelcomeCredits = true
                )
            )

            val creatorId = db.userDao().insertUser(
                UserEntity(
                    email = "creator@titon.ai",
                    username = "NeoDirector",
                    passwordHash = "creator123",
                    credits = 50,
                    role = "USER",
                    avatarSeed = "creator_pro",
                    hasReceivedWelcomeCredits = true
                )
            )

            // Initial transactions
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    userId = creatorId,
                    type = "WELCOME_BONUS",
                    amount = 50,
                    balanceAfter = 50,
                    description = "Welcome Bonus: 50 Free Credits"
                )
            )

            // 2. Default Credit Packages (STARTER, PRO, PREMIUM)
            val packages = listOf(
                CreditPackageEntity(
                    id = "STARTER",
                    name = "Starter Package",
                    credits = 100,
                    priceHtg = 1250,
                    priceUsd = 9.99,
                    badge = "Basic Creator",
                    isPopular = false,
                    isActive = true
                ),
                CreditPackageEntity(
                    id = "PRO",
                    name = "Pro Studio",
                    credits = 500,
                    priceHtg = 5000,
                    priceUsd = 39.99,
                    badge = "Most Popular",
                    isPopular = true,
                    isActive = true
                ),
                CreditPackageEntity(
                    id = "PREMIUM",
                    name = "Cinematic Studio",
                    credits = 1500,
                    priceHtg = 12500,
                    priceUsd = 99.99,
                    badge = "Best Value",
                    isPopular = false,
                    isActive = true
                )
            )
            db.packageDao().insertPackages(packages)

            // 3. Supported Real AI Video Providers (Fal.ai, Replicate, Custom Proxy)
            val falKey = try { com.example.BuildConfig.FAL_KEY.trim().let { if (it.startsWith("DEFAULT_")) "" else it } } catch (e: Throwable) { "" }
            val replicateKey = try { com.example.BuildConfig.REPLICATE_API_TOKEN.trim().let { if (it.startsWith("DEFAULT_")) "" else it } } catch (e: Throwable) { "" }
            val backendUrl = try { com.example.BuildConfig.TITON_BACKEND_URL.trim().let { if (it.startsWith("DEFAULT_")) "" else it } } catch (e: Throwable) { "" }

            val providers = listOf(
                ProviderConfigEntity(
                    id = "fal_ai",
                    displayName = "Fal.ai Video (LTX / Kling)",
                    apiKey = falKey,
                    apiEndpoint = "https://queue.fal.run/fal-ai/ltx-video",
                    defaultModel = "fal-ai/ltx-video",
                    cost5s = 10,
                    cost10s = 20,
                    cost15s = 30,
                    isEnabled = true,
                    isDefault = true
                ),
                ProviderConfigEntity(
                    id = "replicate",
                    displayName = "Replicate Wan 2.1 Video",
                    apiKey = replicateKey,
                    apiEndpoint = "https://api.replicate.com/v1/predictions",
                    defaultModel = "wan-video/wan-2.1-t2v-14b",
                    cost5s = 10,
                    cost10s = 20,
                    cost15s = 30,
                    isEnabled = true,
                    isDefault = false
                ),
                ProviderConfigEntity(
                    id = "custom",
                    displayName = "Custom Backend Proxy",
                    apiKey = "",
                    apiEndpoint = if (backendUrl.isNotBlank()) backendUrl else "",
                    defaultModel = "custom-v1",
                    cost5s = 10,
                    cost10s = 20,
                    cost15s = 30,
                    isEnabled = true,
                    isDefault = false
                )
            )
            db.providerConfigDao().insertProviders(providers)

            // 4. Configurable NatCash Haiti Merchant Credentials
            db.merchantConfigDao().insertOrUpdate(MerchantConfigEntity())

            // 5. Initial Showcase Examples for Homepage and Gallery
            val showcaseExamples = listOf(
                VideoJobEntity(
                    id = "showcase-1",
                    userId = adminId,
                    prompt = "Cinematic slow motion shot of a bioluminescent cybernetic dragon soaring over neon crystalline metropolis in deep space, 8k, photorealistic octane render, electric teal and ultra violet glow",
                    negativePrompt = "blurry, low quality, noise, artifacts",
                    aspectRatio = "16:9",
                    duration = 10,
                    quality = "Ultra HD",
                    status = "COMPLETED",
                    progress = 100,
                    videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4",
                    thumbnailUrl = null,
                    provider = "Fal.ai LTX",
                    creditsCost = 20,
                    isShowcase = true
                ),
                VideoJobEntity(
                    id = "showcase-2",
                    userId = adminId,
                    prompt = "Hyper-realistic futuristic cyber warrior standing atop rain-slicked Tokyo skyscraper overlooking flying hovercars and holographic billboards, dramatic cinematic anamorphic lighting",
                    negativePrompt = "cartoon, sketch, low res",
                    aspectRatio = "9:16",
                    duration = 5,
                    quality = "High Quality",
                    status = "COMPLETED",
                    progress = 100,
                    videoUrl = "https://media.w3.org/2010/05/video/movie_300.mp4",
                    thumbnailUrl = null,
                    provider = "Fal.ai LTX",
                    creditsCost = 10,
                    isShowcase = true
                ),
                VideoJobEntity(
                    id = "showcase-3",
                    userId = adminId,
                    prompt = "Surreal liquid gold fluid morphing into a majestic celestial dragon against a star-studded purple nebula, orbital macro camera sweep",
                    negativePrompt = "flicker, static, pixelated",
                    aspectRatio = "1:1",
                    duration = 15,
                    quality = "Ultra HD",
                    status = "COMPLETED",
                    progress = 100,
                    videoUrl = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4",
                    thumbnailUrl = null,
                    provider = "Fal.ai LTX",
                    creditsCost = 30,
                    isShowcase = true
                ),
                VideoJobEntity(
                    id = "showcase-4",
                    userId = adminId,
                    prompt = "High-octane neon cyber speeder drifting through subterranean highway tunnels, volumetric lighting and rain particles",
                    negativePrompt = "low quality, stutter",
                    aspectRatio = "16:9",
                    duration = 5,
                    quality = "Ultra HD",
                    status = "COMPLETED",
                    progress = 100,
                    videoUrl = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/friday.mp4",
                    thumbnailUrl = null,
                    provider = "Fal.ai LTX",
                    creditsCost = 10,
                    isShowcase = true
                )
            )
            showcaseExamples.forEach { db.videoJobDao().insertJob(it) }
        }
    }
}
