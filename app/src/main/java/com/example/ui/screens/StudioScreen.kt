package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TitonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: TitonViewModel
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val userVideos by viewModel.userVideos.collectAsState()

    val prompt by viewModel.prompt.collectAsState()
    val negativePrompt by viewModel.negativePrompt.collectAsState()
    val isImageToVideo by viewModel.isImageToVideo.collectAsState()
    val selectedImageUrl by viewModel.selectedImageUrl.collectAsState()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()
    val selectedDuration by viewModel.selectedDuration.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val selectedProviderId by viewModel.selectedProviderId.collectAsState()

    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationProgress by viewModel.generationProgress.collectAsState()
    val generationError by viewModel.generationError.collectAsState()

    val isTestingConnection by viewModel.isTestingConnection.collectAsState()
    val connectionTestMessage by viewModel.connectionTestMessage.collectAsState()
    val connectionTestSuccess by viewModel.connectionTestSuccess.collectAsState()
    val verifiedProviders by viewModel.verifiedProviders.collectAsState()

    var showNegativePrompt by remember { mutableStateOf(false) }
    var showConfigureKeyDialog by remember { mutableStateOf(false) }

    val currentProvider = providers.find { it.id == selectedProviderId } ?: providers.firstOrNull()
    val effectiveKey = currentProvider?.let { viewModel.getEffectiveApiKey(it) } ?: ""
    val isApiKeyConfigured = effectiveKey.isNotBlank()
    val isVerified = verifiedProviders.contains(selectedProviderId)

    val creditCost = when (selectedDuration) {
        5 -> currentProvider?.cost5s ?: 10
        10 -> currentProvider?.cost10s ?: 20
        15 -> currentProvider?.cost15s ?: 30
        else -> 10
    }

    val userCredits = currentUser?.credits ?: 0
    val hasEnoughCredits = userCredits >= creditCost
    val latestCompletedVideo = userVideos.firstOrNull { it.status == "COMPLETED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TitonDarkVoid)
            .padding(horizontal = 16.dp)
            .testTag("studio_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Studio Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Video Studio",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Turn text & imagery into cinematic motion",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TitonSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$userCredits credits",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2. AI Video Provider & Live Connection Tester Card
        item {
            Surface(
                color = TitonSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isApiKeyConfigured) ElectricBlue.copy(alpha = 0.5f) else StatusPending.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isApiKeyConfigured) ElectricBlue.copy(alpha = 0.15f)
                                        else StatusPending.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = if (isApiKeyConfigured) ElectricBlue else StatusPending,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "AI Video Generation Engine",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = currentProvider?.displayName ?: "Real AI Engine",
                                    color = ElectricBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        val (badgeText, badgeColor) = when {
                            isVerified -> "AUTHENTICATED (VERIFIED)" to StatusSuccess
                            isApiKeyConfigured -> "KEY ACTIVE" to ElectricBlue
                            else -> "FAL_KEY REQUIRED" to StatusPending
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Engine Selection Chips
                    Text(text = "Select Documented Video Engine:", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(providers) { provider ->
                            val isSelected = selectedProviderId == provider.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NeonViolet.copy(alpha = 0.4f) else TitonSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ElectricBlue else TitonBorder
                                ),
                                modifier = Modifier.clickable { viewModel.setProvider(provider.id) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = if (isSelected) ElectricBlue else TextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = provider.displayName,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Connection Test Banner (if test was performed)
                    connectionTestMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (connectionTestSuccess) {
                                true -> StatusSuccess.copy(alpha = 0.12f)
                                false -> StatusError.copy(alpha = 0.12f)
                                else -> TitonSurfaceVariant
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (connectionTestSuccess) {
                                    true -> StatusSuccess.copy(alpha = 0.6f)
                                    false -> StatusError.copy(alpha = 0.6f)
                                    else -> ElectricBlue.copy(alpha = 0.4f)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (connectionTestSuccess) {
                                        true -> Icons.Default.CheckCircle
                                        false -> Icons.Default.ErrorOutline
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    tint = when (connectionTestSuccess) {
                                        true -> StatusSuccess
                                        false -> StatusError
                                        else -> ElectricBlue
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    color = when (connectionTestSuccess) {
                                        true -> StatusSuccess
                                        false -> StatusError
                                        else -> TextPrimary
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Action buttons: Test Connection and Configure Key
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.testCurrentProviderConnection() },
                            enabled = !isTestingConnection,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_api_connection_button")
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    color = ElectricBlue,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test API Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { showConfigureKeyDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TitonSurfaceVariant,
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("configure_credentials_button")
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Configure Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Mode Selector: Text-to-Video vs Image-to-Video
        item {
            Surface(
                color = TitonSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    val textTabModifier = if (!isImageToVideo) {
                        Modifier.background(Brush.linearGradient(listOf(ElectricBlue, NeonPurple)))
                    } else {
                        Modifier.background(Color.Transparent)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .then(textTabModifier)
                            .clickable { viewModel.setImageToVideoMode(false) }
                            .padding(vertical = 10.dp)
                            .testTag("tab_text_to_video"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = null,
                                tint = if (!isImageToVideo) Color.Black else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Text-to-Video",
                                color = if (!isImageToVideo) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    val imageTabModifier = if (isImageToVideo) {
                        Modifier.background(Brush.linearGradient(listOf(ElectricBlue, NeonPurple)))
                    } else {
                        Modifier.background(Color.Transparent)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .then(imageTabModifier)
                            .clickable { viewModel.setImageToVideoMode(true) }
                            .padding(vertical = 10.dp)
                            .testTag("tab_image_to_video"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = if (isImageToVideo) Color.Black else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Image-to-Video",
                                color = if (isImageToVideo) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Image Upload & Keyframe Selection (If Image-to-Video enabled)
        if (isImageToVideo) {
            item {
                Surface(
                    color = TitonSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Reference Keyframe Image",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Select a keyframe or enter an image URL to animate into motion",
                            color = TextMuted,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val sampleImages = listOf(
                            Pair("Cyberpunk City", "https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=500"),
                            Pair("Sci-Fi Robot", "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=500"),
                            Pair("Cosmic Nebula", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=500")
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(sampleImages) { (label, url) ->
                                val isSelected = selectedImageUrl == url
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) NeonViolet.copy(alpha = 0.4f) else TitonSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) ElectricBlue else TitonBorder
                                    ),
                                    modifier = Modifier.clickable { viewModel.setImageToVideoMode(true, url) }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Image,
                                            contentDescription = null,
                                            tint = if (isSelected) ElectricBlue else TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            color = if (isSelected) ElectricBlue else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        var customUrlInput by remember(selectedImageUrl) { mutableStateOf(selectedImageUrl ?: "") }
                        OutlinedTextField(
                            value = customUrlInput,
                            onValueChange = {
                                customUrlInput = it
                                viewModel.setImageToVideoMode(true, it.trim().takeIf { s -> s.isNotEmpty() })
                            },
                            label = { Text("Or Paste Custom Image URL", fontSize = 11.sp) },
                            placeholder = { Text("https://example.com/character.jpg", color = TextMuted, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = TitonBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 5. Large Prompt Input Box
        item {
            Column {
                Text(
                    text = "Video Prompt",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { viewModel.setPrompt(it) },
                    placeholder = {
                        Text(
                            text = "Describe your cinematic scene... e.g. Hyper-realistic aerial drone shot of neon cyberpunk skyscrapers in heavy rain, glowing volumetric lights reflecting on wet pavement, 8k resolution, cinematic motion",
                            color = TextMuted,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    },
                    minLines = 4,
                    maxLines = 7,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_prompt_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TitonSurface,
                        unfocusedContainerColor = TitonSurface,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = TitonBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Prompt Enhancer Chips
                val promptChips = listOf(
                    "Cinematic 8K Octane",
                    "Anamorphic Lens Flare",
                    "Slow Motion 120fps",
                    "Cyberpunk Atmosphere",
                    "Volumetric Lighting",
                    "Hyper-lapse Drone Sweep"
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(promptChips) { chip ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = TitonSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                            modifier = Modifier.clickable {
                                val current = prompt
                                val addition = if (current.isBlank()) chip else "$current, $chip"
                                viewModel.setPrompt(addition)
                            }
                        ) {
                            Text(
                                text = "+ $chip",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Negative Prompt Accordion
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showNegativePrompt = !showNegativePrompt }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (showNegativePrompt) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showNegativePrompt) "Hide Negative Prompt" else "Advanced: Negative Prompt",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                if (showNegativePrompt) {
                    OutlinedTextField(
                        value = negativePrompt,
                        onValueChange = { viewModel.negativePrompt.value = it },
                        placeholder = { Text("What to avoid (e.g. blurry, low quality, artifacts, watermark)", color = TextMuted, fontSize = 12.sp) },
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TitonSurface,
                            unfocusedContainerColor = TitonSurface,
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = TitonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 6. Video Settings (Aspect Ratio, Duration, Quality)
        item {
            Surface(
                color = TitonSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Video Settings",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Aspect Ratios: 16:9, 9:16, 1:1
                    Text(text = "Aspect Ratio", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("16:9", "Landscape", Icons.Default.Crop169),
                            Triple("9:16", "Portrait", Icons.Default.StayCurrentPortrait),
                            Triple("1:1", "Square", Icons.Default.CropSquare)
                        ).forEach { (ratio, label, icon) ->
                            val isSelected = selectedAspectRatio == ratio
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NeonViolet.copy(alpha = 0.35f) else TitonSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ElectricBlue else TitonBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setAspectRatio(ratio) }
                                    .testTag("ratio_$ratio")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) ElectricBlue else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ratio,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = label,
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Durations: 5s, 10s, 15s
                    Text(text = "Duration", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair(5, "10 Credits"),
                            Pair(10, "20 Credits"),
                            Pair(15, "30 Credits")
                        ).forEach { (seconds, costLabel) ->
                            val isSelected = selectedDuration == seconds
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NeonViolet.copy(alpha = 0.35f) else TitonSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ElectricBlue else TitonBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setDuration(seconds) }
                                    .testTag("duration_${seconds}s")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${seconds}s",
                                        color = if (isSelected) ElectricBlue else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = costLabel,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quality: Standard, High Quality, Ultra HD
                    Text(text = "Quality Output", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Standard", "High Quality", "Ultra HD").forEach { q ->
                            val isSelected = selectedQuality == q
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NeonViolet.copy(alpha = 0.35f) else TitonSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ElectricBlue else TitonBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setQuality(q) }
                                    .testTag("quality_$q")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = q,
                                        color = if (isSelected) ElectricBlue else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 7. Generation Progress Tracker or Error
        item {
            if (isGenerating) {
                Surface(
                    color = TitonSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("generation_progress_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = ElectricBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Rendering Real AI Video ($generationProgress%)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { generationProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ElectricBlue,
                            trackColor = TitonSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Processing neural video diffusion frames on ${currentProvider?.displayName}...",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (generationError != null) {
                Surface(
                    color = StatusError.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusError),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("generation_error_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = StatusError,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI Video Generation Notice",
                                    color = StatusError,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = generationError ?: "",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        val isAuthRelated = generationError?.let {
                            it.contains("401") || it.contains("403") || it.contains("FAL_KEY") ||
                            it.contains("Authentication") || it.contains("Configured")
                        } ?: false

                        if (isAuthRelated) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showConfigureKeyDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricBlue,
                                    contentColor = Color.Black
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Configure FAL_KEY Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 8. Latest Generated Video Card (if any video completed)
        if (latestCompletedVideo != null && !isGenerating) {
            item {
                Surface(
                    color = TitonSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.playVideo(latestCompletedVideo) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusPill(status = "COMPLETED")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ready to Play",
                                    color = ElectricBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = latestCompletedVideo.prompt,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { viewModel.playVideo(latestCompletedVideo) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricBlue,
                                    contentColor = Color.Black
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { viewModel.downloadVideo(context, latestCompletedVideo) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download", tint = ElectricBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // 9. Generate Button & Credit Cost
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cost: $creditCost Credits (deducted only on success)",
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    if (!hasEnoughCredits) {
                        Text(
                            text = "Insufficient credits",
                            color = StatusError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (!hasEnoughCredits) {
                            viewModel.navigateTo(AppScreen.PRICING)
                        } else if (!isApiKeyConfigured) {
                            showConfigureKeyDialog = true
                        } else {
                            viewModel.generateVideo()
                        }
                    },
                    enabled = !isGenerating,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasEnoughCredits) ElectricBlue else NeonPurple,
                        contentColor = if (hasEnoughCredits) Color.Black else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_video_button")
                ) {
                    Icon(
                        imageVector = if (hasEnoughCredits) Icons.Default.AutoAwesome else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasEnoughCredits) {
                            if (!isApiKeyConfigured) "Configure FAL_KEY to Generate ($creditCost Credits)"
                            else "Generate AI Video ($creditCost Credits)"
                        } else "Buy Credits via NatCash",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Modal: Quick Configure Provider API Credentials
    if (showConfigureKeyDialog && currentProvider != null) {
        var keyInput by remember(currentProvider) { mutableStateOf(currentProvider.apiKey) }
        var endpointInput by remember(currentProvider) { mutableStateOf(currentProvider.apiEndpoint) }
        var isKeyVisible by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showConfigureKeyDialog = false }) {
            Surface(
                color = TitonDarkVoid,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Configure ${currentProvider.displayName}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = { showConfigureKeyDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter your official API key or configure FAL_KEY / REPLICATE_API_TOKEN in the AI Studio Secrets panel. The model endpoint is fal-ai/ltx-video.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("FAL_KEY / Secret Token", fontSize = 11.sp) },
                        placeholder = { Text("Paste secret key", color = TextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isKeyVisible) "Hide Key" else "Show Key",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = TitonBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = endpointInput,
                        onValueChange = { endpointInput = it },
                        label = { Text("API Endpoint URL", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = TitonBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.saveProviderCredentials(currentProvider.id, keyInput, endpointInput)
                                viewModel.testProviderConnection(currentProvider.copy(apiKey = keyInput.trim(), apiEndpoint = endpointInput.trim()))
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save & Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.saveProviderCredentials(currentProvider.id, keyInput, endpointInput)
                                showConfigureKeyDialog = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
