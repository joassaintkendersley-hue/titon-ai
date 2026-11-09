package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.AppDatabase
import com.example.data.repository.TitonRepository
import com.example.ui.components.TitonBottomNav
import com.example.ui.components.TitonTopBar
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.screens.*
import com.example.ui.theme.TitonAiTheme
import com.example.ui.theme.TitonDarkVoid
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TitonViewModel
import com.example.ui.viewmodel.TitonViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: TitonViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = TitonRepository(database)
        TitonViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TitonAiTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                val currentVideo by viewModel.currentPlayingVideo.collectAsState()
                val checkoutPkg by viewModel.checkoutPackage.collectAsState()
                val snackbarMessage by viewModel.snackbarMessage.collectAsState()

                var activeLegalModal by remember { mutableStateOf<String?>(null) }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                // Handle back button: return to Home if on sub-screens
                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    viewModel.navigateTo(AppScreen.HOME)
                }

                // Show snackbars when message changes
                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { msg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(msg)
                            viewModel.dismissSnackbar()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = TitonDarkVoid,
                    topBar = {
                        TitonTopBar(
                            currentUser = currentUser,
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) },
                            onOpenAuth = { viewModel.navigateTo(AppScreen.AUTH) }
                        )
                    },
                    bottomBar = {
                        TitonBottomNav(
                            currentScreen = currentScreen,
                            isAdmin = currentUser?.role == "ADMIN",
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(TitonDarkVoid)
                    ) {
                        when (currentScreen) {
                            AppScreen.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onOpenLegalModal = { activeLegalModal = it }
                            )
                            AppScreen.STUDIO -> StudioScreen(viewModel = viewModel)
                            AppScreen.MY_VIDEOS -> MyVideosScreen(viewModel = viewModel)
                            AppScreen.PRICING -> PricingScreen(viewModel = viewModel)
                            AppScreen.PAYMENTS -> NatcashPaymentsScreen(viewModel = viewModel)
                            AppScreen.ADMIN -> AdminScreen(viewModel = viewModel)
                            AppScreen.PROFILE -> ProfileScreen(
                                viewModel = viewModel,
                                onOpenLegalModal = { activeLegalModal = it }
                            )
                            AppScreen.AUTH -> AuthScreen(viewModel = viewModel)
                        }
                    }
                }

                // Video Player Modal
                currentVideo?.let { video ->
                    VideoPlayerDialog(
                        video = video,
                        onDismiss = { viewModel.closeVideoPlayer() },
                        onDownload = { viewModel.downloadVideo(context, video) },
                        onUsePromptInStudio = { prompt ->
                            viewModel.setPrompt(prompt)
                            viewModel.setAspectRatio(video.aspectRatio)
                            viewModel.navigateTo(AppScreen.STUDIO)
                        }
                    )
                }

                // NatCash Haiti Checkout Dialog
                checkoutPkg?.let { pkg ->
                    NatcashPaymentDialog(
                        pkg = pkg,
                        viewModel = viewModel,
                        onDismiss = { viewModel.closeNatcashCheckout() }
                    )
                }

                // Legal Dialogs (About, Help, Privacy, Terms)
                activeLegalModal?.let { type ->
                    LegalModal(
                        type = type,
                        onDismiss = { activeLegalModal = null }
                    )
                }
            }
        }
    }
}
