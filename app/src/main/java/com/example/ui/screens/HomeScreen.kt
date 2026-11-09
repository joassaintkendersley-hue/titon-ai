package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CreditPackageEntity
import com.example.data.model.VideoJobEntity
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TitonViewModel

@Composable
fun HomeScreen(
    viewModel: TitonViewModel,
    onOpenLegalModal: (String) -> Unit
) {
    val showcaseVideos by viewModel.showcaseVideos.collectAsState()
    val activePackages by viewModel.activePackages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TitonDarkVoid)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. HERO BANNER SECTION
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                // Background Hero Artwork
                Image(
                    painter = painterResource(id = R.drawable.titon_hero),
                    contentDescription = "TITON AI Cinematic Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    TitonDarkVoid.copy(alpha = 0.4f),
                                    TitonDarkVoid.copy(alpha = 0.8f),
                                    TitonDarkVoid
                                )
                            )
                        )
                )

                // Hero Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    // Slogan badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = NeonViolet.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TURN YOUR IMAGINATION INTO VIDEOS",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Create Amazing\nAI Videos",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Transform your imagination into cinematic videos with artificial intelligence. Powered by Seedance neural architecture.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons: Start Creating & Explore Features
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.STUDIO) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp)
                                .testTag("start_creating_hero_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoCall,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start Creating",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(AppScreen.PRICING) },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("explore_features_button")
                        ) {
                            Text(
                                text = "Get Credits",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. WELCOME BONUS CALLOUT BANNER
        item {
            Surface(
                color = TitonSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(ElectricBlue, NeonMagenta))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(ElectricBlue, NeonPurple))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "50 FREE WELCOME CREDITS",
                            color = ElectricBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Every new account receives 50 free credits to test text-to-video & image-to-video generations.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    if (currentUser == null) {
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.AUTH) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonPurple,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("claim_free_credits_button")
                        ) {
                            Text(text = "Claim", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. SHOWCASE AI VIDEO EXAMPLES
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI Video Showcase",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Created with TITON AI & Seedance technology",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    TextButton(onClick = { viewModel.navigateTo(AppScreen.MY_VIDEOS) }) {
                        Text(text = "View All", color = ElectricBlue, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (showcaseVideos.isEmpty()) {
                    Surface(
                        color = TitonSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Showcase Videos Yet",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Generate videos in the Studio to showcase them here.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.STUDIO) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.Black)
                            ) {
                                Text("Open Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(showcaseVideos) { video ->
                            ShowcaseVideoCard(
                                video = video,
                                onPlay = { viewModel.playVideo(video) },
                                onRemix = {
                                    viewModel.setPrompt(video.prompt)
                                    viewModel.setAspectRatio(video.aspectRatio)
                                    viewModel.navigateTo(AppScreen.STUDIO)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 4. CORE PLATFORM FEATURES SECTION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Engine Capabilities",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Professional cinematic AI toolset designed for creators",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                val features = listOf(
                    Triple(Icons.Default.TextFields, "Text-to-Video", "Turn descriptive natural language prompts into fluid, hyper-realistic video scenes."),
                    Triple(Icons.Default.AddPhotoAlternate, "Image-to-Video", "Animate still images, portraits, and concept artwork with dynamic physics and camera motion."),
                    Triple(Icons.Default.HighQuality, "Multi-Format & 4K", "Generate in 16:9 widescreen, 9:16 vertical reels, and 1:1 square up to Ultra HD quality."),
                    Triple(Icons.Default.Payment, "NatCash Haiti Integration", "Direct local payment verification in Haitian Gourdes (HTG) with automatic credit top-up.")
                )

                features.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { (icon, title, desc) ->
                            Surface(
                                color = TitonSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(140.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(TitonSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = title,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = desc,
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. CREDIT PACKAGES PREVIEW SECTION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Credit Packages",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Recharge your credits via NatCash Haiti or Card",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    TextButton(onClick = { viewModel.navigateTo(AppScreen.PRICING) }) {
                        Text(text = "View Details", color = ElectricBlue, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                activePackages.forEach { pkg ->
                    CreditPackageRowCard(
                        pkg = pkg,
                        onBuy = { viewModel.openNatcashCheckout(pkg) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // 6. FOOTER & ABOUT TITON AI SECTION
        item {
            Surface(
                color = TitonDarkPurple,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .border(1.dp, TitonBorder, RoundedCornerShape(0.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MovieFilter,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TITON AI",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Turn Your Imagination Into Videos.\nOfficial Seedance AI video generation platform.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legal links
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "About",
                            color = ElectricBlue,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { onOpenLegalModal("ABOUT") }
                        )
                        Text(
                            text = "Help Center",
                            color = ElectricBlue,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { onOpenLegalModal("HELP") }
                        )
                        Text(
                            text = "Privacy Policy",
                            color = ElectricBlue,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { onOpenLegalModal("PRIVACY") }
                        )
                        Text(
                            text = "Terms of Service",
                            color = ElectricBlue,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { onOpenLegalModal("TERMS") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "© 2026 TITON AI Inc. All rights reserved.",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ShowcaseVideoCard(
    video: VideoJobEntity,
    onPlay: () -> Unit,
    onRemix: () -> Unit
) {
    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
        modifier = Modifier
            .width(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPlay() }
            .testTag("showcase_card_${video.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(TitonSurfaceVariant, NeonViolet.copy(alpha = 0.4f), TitonDarkVoid)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Play Icon Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.5.dp, Brush.linearGradient(listOf(ElectricBlue, NeonMagenta)), CircleShape)
                        .clickable { onPlay() }
                        .testTag("play_button_${video.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = ElectricBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Top Tag: Watch Cinematic
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "PLAY",
                            color = ElectricBlue,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Aspect Ratio & Duration tag
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "${video.aspectRatio} • ${video.duration}s",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonViolet.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = video.quality,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = video.prompt,
                    color = TextPrimary,
                    fontSize = 11.5.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.provider,
                        color = NeonMagenta,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedButton(
                        onClick = onRemix,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(text = "Remix", color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreditPackageRowCard(
    pkg: CreditPackageEntity,
    onBuy: () -> Unit
) {
    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (pkg.isPopular) ElectricBlue.copy(alpha = 0.6f) else TitonBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pkg.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (pkg.badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonViolet.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = pkg.badge,
                                color = ElectricBlue,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${pkg.credits} Credits • ${pkg.priceHtg} HTG (~$${pkg.priceUsd} USD)",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onBuy,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pkg.isPopular) ElectricBlue else NeonPurple,
                    contentColor = if (pkg.isPopular) Color.Black else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("buy_package_${pkg.id}")
            ) {
                Text(text = "Buy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
