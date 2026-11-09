package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitonTopBar(
    currentUser: UserEntity?,
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onOpenAuth: () -> Unit
) {
    Surface(
        color = TitonDarkPurple.copy(alpha = 0.95f),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(TitonBorder, ElectricBlue.copy(alpha = 0.3f), NeonPurple.copy(alpha = 0.3f))),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigate(AppScreen.HOME) }
                    .padding(4.dp)
                    .testTag("brand_logo_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(listOf(ElectricBlue, NeonPurple))
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MovieFilter,
                        contentDescription = "TITON AI Logo",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TITON",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI",
                            color = ElectricBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        text = "Seedance Engine",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right side: Credits Badge & User Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentUser != null) {
                    // Credit Balance Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = TitonSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onNavigate(AppScreen.PRICING) }
                            .testTag("credit_balance_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Credits",
                                tint = ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentUser.credits}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = " pts",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Admin Badge if Admin
                    if (currentUser.role == "ADMIN") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonViolet.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple),
                            modifier = Modifier
                                .clickable { onNavigate(AppScreen.ADMIN) }
                                .testTag("admin_badge_header")
                        ) {
                            Text(
                                text = "ADMIN",
                                color = NeonMagenta,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Profile / Settings Avatar Button
                    IconButton(
                        onClick = { onNavigate(AppScreen.PROFILE) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("profile_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(NeonViolet, ElectricBlue)))
                                .padding(1.5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(TitonSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.username.take(1).uppercase(),
                                    color = ElectricBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    // Login / Sign Up Button
                    Button(
                        onClick = onOpenAuth,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("auth_login_signup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

data class NavItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val tag: String,
    val requiresAdmin: Boolean = false
)

@Composable
fun TitonBottomNav(
    currentScreen: AppScreen,
    isAdmin: Boolean,
    onNavigate: (AppScreen) -> Unit
) {
    val items = listOf(
        NavItem(AppScreen.HOME, "Home", Icons.Default.Home, "nav_home"),
        NavItem(AppScreen.STUDIO, "Studio", Icons.Default.AutoFixHigh, "nav_studio"),
        NavItem(AppScreen.MY_VIDEOS, "Videos", Icons.Default.VideoLibrary, "nav_videos"),
        NavItem(AppScreen.PRICING, "Credits", Icons.Default.AddShoppingCart, "nav_credits"),
        NavItem(AppScreen.PAYMENTS, "NatCash", Icons.Default.Payments, "nav_payments")
    ) + if (isAdmin) listOf(NavItem(AppScreen.ADMIN, "Admin", Icons.Default.AdminPanelSettings, "nav_admin", true)) else emptyList()

    Surface(
        color = TitonDarkVoid,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(
                width = 1.dp,
                color = TitonBorder,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentScreen == item.screen
                val activeBrush = Brush.linearGradient(listOf(ElectricBlue, NeonPurple))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigate(item.screen) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag(item.tag)
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(activeBrush)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.title,
                        color = if (selected) ElectricBlue else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val (bgColor, textColor, icon) = when (status.uppercase()) {
        "COMPLETED", "APPROVED" -> Triple(StatusSuccess.copy(alpha = 0.15f), StatusSuccess, Icons.Default.CheckCircle)
        "PROCESSING", "PENDING" -> Triple(StatusPending.copy(alpha = 0.15f), StatusPending, Icons.Default.HourglassTop)
        "FAILED", "REJECTED" -> Triple(StatusError.copy(alpha = 0.15f), StatusError, Icons.Default.Cancel)
        else -> Triple(TitonSurfaceVariant, TextSecondary, Icons.Default.Info)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.4f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.uppercase(),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
