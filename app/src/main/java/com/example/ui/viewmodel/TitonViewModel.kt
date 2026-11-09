package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.TitonRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    STUDIO,
    MY_VIDEOS,
    PRICING,
    PAYMENTS,
    ADMIN,
    PROFILE,
    AUTH
}

class TitonViewModel(
    private val repository: TitonRepository
) : ViewModel() {

    // Current navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // User session
    val currentUserId = repository.currentUserId
    val currentUser: StateFlow<UserEntity?> = currentUserId.flatMapLatest { id ->
        if (id != null) repository.getCurrentUser(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCount: StateFlow<Int> = repository.getUserCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Credit packages
    val activePackages: StateFlow<List<CreditPackageEntity>> = repository.getActivePackages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPackages: StateFlow<List<CreditPackageEntity>> = repository.getAllPackages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // NatCash Payments
    val userPayments: StateFlow<List<NatcashPaymentEntity>> = currentUserId.flatMapLatest { id ->
        if (id != null) repository.getPaymentsForUser(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<NatcashPaymentEntity>> = repository.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingPaymentsCount: StateFlow<Int> = repository.getPendingPaymentsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val approvedPaymentsCount: StateFlow<Int> = repository.getApprovedPaymentsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val rejectedPaymentsCount: StateFlow<Int> = repository.getRejectedPaymentsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val approvedPayments: StateFlow<List<NatcashPaymentEntity>> = repository.getApprovedPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCreditsSold: StateFlow<Int> = repository.getTotalCreditsSold().map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalRevenueHtg: StateFlow<Int> = repository.getTotalRevenueHtg().map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val merchantConfig: StateFlow<MerchantConfigEntity> = repository.getMerchantConfig()
        .map { it ?: MerchantConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MerchantConfigEntity())

    // Transactions
    val userTransactions: StateFlow<List<TransactionEntity>> = currentUserId.flatMapLatest { id ->
        if (id != null) repository.getTransactionsForUser(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Providers
    val providers: StateFlow<List<ProviderConfigEntity>> = repository.getAllProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Videos
    val userVideos: StateFlow<List<VideoJobEntity>> = currentUserId.flatMapLatest { id ->
        if (id != null) repository.getVideosForUser(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val showcaseVideos: StateFlow<List<VideoJobEntity>> = repository.getShowcaseVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVideos: StateFlow<List<VideoJobEntity>> = repository.getAllVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalVideosCount: StateFlow<Int> = repository.getTotalVideosCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Video Studio Studio State
    var prompt = MutableStateFlow("")
    var negativePrompt = MutableStateFlow("")
    var selectedImageUrl = MutableStateFlow<String?>(null)
    var selectedAspectRatio = MutableStateFlow("16:9")
    var selectedDuration = MutableStateFlow(5)
    var selectedQuality = MutableStateFlow("High Quality")
    var selectedProviderId = MutableStateFlow("fal_ai")
    var isImageToVideo = MutableStateFlow(false)

    var isGenerating = MutableStateFlow(false)
    var generationProgress = MutableStateFlow(0)
    var generationError = MutableStateFlow<String?>(null)
    var currentPlayingVideo = MutableStateFlow<VideoJobEntity?>(null)

    // API Connection Testing States
    var isTestingConnection = MutableStateFlow(false)
    var connectionTestMessage = MutableStateFlow<String?>(null)
    var connectionTestSuccess = MutableStateFlow<Boolean?>(null)
    var verifiedProviders = MutableStateFlow<Set<String>>(emptySet())

    // Checkout Modal State
    var checkoutPackage = MutableStateFlow<CreditPackageEntity?>(null)
    var paymentReferenceInput = MutableStateFlow("")
    var senderPhoneInput = MutableStateFlow("")
    var paymentNoteInput = MutableStateFlow("")
    var isSubmittingPayment = MutableStateFlow(false)

    // User feedback / snackbar
    var snackbarMessage = MutableStateFlow<String?>(null)

    fun showSnackbar(message: String) {
        snackbarMessage.value = message
    }

    fun dismissSnackbar() {
        snackbarMessage.value = null
    }

    // --- Authentication Actions ---
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            repository.loginUser(email, pass).fold(
                onSuccess = { user ->
                    showSnackbar("Welcome back, ${user.username}!")
                    navigateTo(AppScreen.STUDIO)
                },
                onFailure = { err ->
                    showSnackbar(err.message ?: "Login failed")
                }
            )
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            repository.registerUser(name, email, pass).fold(
                onSuccess = { user ->
                    showSnackbar("Account created! 50 Free Welcome Credits added!")
                    navigateTo(AppScreen.STUDIO)
                },
                onFailure = { err ->
                    showSnackbar(err.message ?: "Registration failed")
                }
            )
        }
    }

    fun logout() {
        repository.setCurrentUser(null)
        showSnackbar("Logged out successfully.")
        navigateTo(AppScreen.AUTH)
    }

    fun switchAccount(userId: Long) {
        repository.setCurrentUser(userId)
        showSnackbar("Switched user account.")
    }

    // --- Video Studio Actions ---
    fun setPrompt(text: String) {
        prompt.value = text
    }

    fun setAspectRatio(ratio: String) {
        selectedAspectRatio.value = ratio
    }

    fun setDuration(seconds: Int) {
        selectedDuration.value = seconds
    }

    fun setQuality(q: String) {
        selectedQuality.value = q
    }

    fun setProvider(id: String) {
        selectedProviderId.value = id
    }

    fun setImageToVideoMode(enabled: Boolean, presetImage: String? = null) {
        isImageToVideo.value = enabled
        selectedImageUrl.value = presetImage
    }

    fun playVideo(video: VideoJobEntity) {
        currentPlayingVideo.value = video
    }

    fun closeVideoPlayer() {
        currentPlayingVideo.value = null
    }

    fun generateVideo() {
        val user = currentUser.value
        if (user == null) {
            showSnackbar("Please login or create an account to generate videos.")
            navigateTo(AppScreen.AUTH)
            return
        }

        val promptText = prompt.value.trim()
        if (promptText.isEmpty()) {
            showSnackbar("Please enter a video prompt description.")
            return
        }

        viewModelScope.launch {
            isGenerating.value = true
            generationProgress.value = 10
            generationError.value = null

            repository.requestVideoGeneration(
                userId = user.id,
                prompt = promptText,
                negativePrompt = negativePrompt.value.trim(),
                imageUrl = if (isImageToVideo.value) selectedImageUrl.value else null,
                aspectRatio = selectedAspectRatio.value,
                duration = selectedDuration.value,
                quality = selectedQuality.value,
                providerId = selectedProviderId.value
            ).fold(
                onSuccess = { completedJob ->
                    isGenerating.value = false
                    generationProgress.value = 100
                    currentPlayingVideo.value = completedJob
                    showSnackbar("Cinematic AI video generated successfully!")
                },
                onFailure = { err ->
                    isGenerating.value = false
                    generationProgress.value = 0
                    generationError.value = err.message
                    showSnackbar(err.message ?: "Generation failed")
                }
            )
        }
    }

    fun downloadVideo(context: Context, video: VideoJobEntity) {
        val url = video.videoUrl
        if (url.isNullOrBlank()) {
            showSnackbar("Video file is not ready for download.")
            return
        }

        viewModelScope.launch {
            showSnackbar("Downloading ${video.duration}s cinematic video...")
            repository.downloadVideo(context, url, video.prompt).fold(
                onSuccess = { file ->
                    showSnackbar("Saved to Downloads: ${file.name}")
                },
                onFailure = { err ->
                    showSnackbar("Download error: ${err.localizedMessage ?: "Failed"}")
                }
            )
        }
    }

    fun deleteVideo(jobId: String) {
        viewModelScope.launch {
            repository.deleteVideo(jobId)
            showSnackbar("Video deleted.")
        }
    }

    // --- NatCash Payment Flow (Haiti) ---
    fun openNatcashCheckout(pkg: CreditPackageEntity) {
        val user = currentUser.value
        if (user == null) {
            showSnackbar("Please login to purchase credit packages.")
            navigateTo(AppScreen.AUTH)
            return
        }
        checkoutPackage.value = pkg
        paymentReferenceInput.value = ""
        senderPhoneInput.value = ""
        paymentNoteInput.value = ""
    }

    fun closeNatcashCheckout() {
        checkoutPackage.value = null
    }

    fun submitNatcashPayment() {
        val user = currentUser.value ?: return
        val pkg = checkoutPackage.value ?: return
        val ref = paymentReferenceInput.value.trim()
        val phone = senderPhoneInput.value.trim()

        if (ref.isEmpty()) {
            showSnackbar("Please enter your NatCash Transaction ID / Reference.")
            return
        }
        if (phone.isEmpty()) {
            showSnackbar("Please enter your NatCash sender phone number.")
            return
        }

        viewModelScope.launch {
            isSubmittingPayment.value = true
            repository.submitNatcashPayment(
                userId = user.id,
                userEmail = user.email,
                pkg = pkg,
                transactionReference = ref,
                senderPhone = phone,
                userNote = paymentNoteInput.value.trim()
            ).fold(
                onSuccess = {
                    isSubmittingPayment.value = false
                    checkoutPackage.value = null
                    showSnackbar("NatCash payment submitted! Awaiting administrator verification.")
                    navigateTo(AppScreen.PAYMENTS)
                },
                onFailure = { err ->
                    isSubmittingPayment.value = false
                    showSnackbar(err.message ?: "Submission failed")
                }
            )
        }
    }

    // --- Admin Dashboard Actions ---
    fun approvePayment(paymentId: String, adminNotes: String? = null) {
        viewModelScope.launch {
            repository.approveNatcashPayment(paymentId, adminNotes).fold(
                onSuccess = {
                    showSnackbar("Payment approved! Credits automatically credited to user.")
                },
                onFailure = { err ->
                    showSnackbar(err.message ?: "Failed to approve payment")
                }
            )
        }
    }

    fun rejectPayment(paymentId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectNatcashPayment(paymentId, reason).fold(
                onSuccess = {
                    showSnackbar("Payment marked as REJECTED.")
                },
                onFailure = { err ->
                    showSnackbar(err.message ?: "Failed to reject payment")
                }
            )
        }
    }

    fun grantCredits(userId: Long, amount: Int, reason: String) {
        viewModelScope.launch {
            repository.grantAdminCredits(userId, amount, reason)
            showSnackbar("Granted $amount credits to user.")
        }
    }

    fun toggleUserAdminRole(userId: Long, currentRole: String) {
        viewModelScope.launch {
            val newRole = if (currentRole == "ADMIN") "USER" else "ADMIN"
            repository.updateUserRole(userId, newRole)
            showSnackbar("User role updated to $newRole.")
        }
    }

    fun updatePackage(pkg: CreditPackageEntity) {
        viewModelScope.launch {
            repository.updatePackage(pkg)
            showSnackbar("Package '${pkg.name}' updated.")
        }
    }

    fun updateProvider(provider: ProviderConfigEntity) {
        viewModelScope.launch {
            repository.updateProviderConfig(provider)
            showSnackbar("Provider '${provider.displayName}' saved.")
        }
    }

    fun getEffectiveApiKey(provider: ProviderConfigEntity): String = repository.getEffectiveApiKey(provider)

    fun testCurrentProviderConnection() {
        val providerId = selectedProviderId.value
        val provider = providers.value.find { it.id == providerId } ?: providers.value.firstOrNull()
        if (provider != null) {
            testProviderConnection(provider)
        } else {
            showSnackbar("No AI provider selected.")
        }
    }

    fun testProviderConnection(provider: ProviderConfigEntity) {
        viewModelScope.launch {
            isTestingConnection.value = true
            connectionTestMessage.value = "Testing connection to ${provider.displayName}..."
            connectionTestSuccess.value = null

            val apiKey = repository.getEffectiveApiKey(provider)
            repository.testApiConnection(provider, apiKey).fold(
                onSuccess = { msg ->
                    isTestingConnection.value = false
                    connectionTestSuccess.value = true
                    connectionTestMessage.value = msg
                    verifiedProviders.value = verifiedProviders.value + provider.id
                    showSnackbar(msg)
                },
                onFailure = { err ->
                    isTestingConnection.value = false
                    connectionTestSuccess.value = false
                    val errMsg = err.message ?: "Connection test failed."
                    connectionTestMessage.value = errMsg
                    verifiedProviders.value = verifiedProviders.value - provider.id
                    showSnackbar(errMsg)
                }
            )
        }
    }

    fun saveProviderCredentials(providerId: String, apiKey: String, endpoint: String = "") {
        viewModelScope.launch {
            val provider = providers.value.find { it.id == providerId }
            if (provider != null) {
                val updated = provider.copy(
                    apiKey = apiKey.trim(),
                    apiEndpoint = if (endpoint.isNotBlank()) endpoint.trim() else provider.apiEndpoint
                )
                repository.updateProviderConfig(updated)
                verifiedProviders.value = verifiedProviders.value - providerId
                showSnackbar("Credentials saved for ${provider.displayName}.")
            }
        }
    }

    fun updateMerchantConfig(phone: String, name: String, ussd: String, instructions: String) {
        viewModelScope.launch {
            val config = MerchantConfigEntity(
                merchantPhone = phone.trim(),
                merchantName = name.trim(),
                ussdCode = ussd.trim(),
                instructions = instructions.trim(),
                updatedAt = System.currentTimeMillis()
            )
            repository.updateMerchantConfig(config)
            showSnackbar("NatCash merchant credentials updated.")
        }
    }
}

class TitonViewModelFactory(
    private val repository: TitonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TitonViewModel::class.java)) {
            return TitonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
