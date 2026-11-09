package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditPackageEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TitonViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PricingScreen(
    viewModel: TitonViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activePackages by viewModel.activePackages.collectAsState()
    val userTransactions by viewModel.userTransactions.collectAsState()

    var showTransactions by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TitonDarkVoid)
            .padding(horizontal = 16.dp)
            .testTag("pricing_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Balance Overview Card
        item {
            Surface(
                color = TitonSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(ElectricBlue, NeonPurple))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Credit Balance",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NatCashGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NatCashGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = NatCashGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "NatCash Haiti Enabled",
                                    color = NatCashGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${currentUser?.credits ?: 0}",
                            color = TextPrimary,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Credits",
                            color = ElectricBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "5s Video = 10 credits  •  10s Video = 20 credits  •  15s Video = 30 credits",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.STUDIO) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Use in Studio", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(AppScreen.PAYMENTS) },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NatCashGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NatCashGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("My NatCash Logs", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. Packages Header
        item {
            Column {
                Text(
                    text = "Credit Packages",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose a credit tier. Pay securely with NatCash in Haiti or Card.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 3. Three Configurable Packages (Starter, Pro, Premium)
        items(activePackages) { pkg ->
            CreditPackageDetailedCard(
                pkg = pkg,
                onBuyWithNatcash = { viewModel.openNatcashCheckout(pkg) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. Free Credits Reminder
        item {
            Surface(
                color = TitonSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "50 Free Welcome Credits are credited once per user upon registration. Refund guarantee: if a video generation fails, credits are instantly refunded back to your balance.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 5. Transaction History Ledger Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTransactions = !showTransactions }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History (${userTransactions.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = if (showTransactions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }

        if (showTransactions) {
            if (userTransactions.isEmpty()) {
                item {
                    Text(
                        text = "No credit transactions recorded yet.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(userTransactions) { tx ->
                    TransactionRow(tx = tx)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CreditPackageDetailedCard(
    pkg: CreditPackageEntity,
    onBuyWithNatcash: () -> Unit
) {
    val isPopular = pkg.isPopular
    val borderBrush = if (isPopular) {
        Brush.linearGradient(listOf(ElectricBlue, NeonMagenta))
    } else {
        Brush.linearGradient(listOf(TitonBorder, TitonBorder))
    }

    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(if (isPopular) 1.5.dp else 1.dp, borderBrush),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("package_card_${pkg.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = pkg.name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "${pkg.credits} AI Video Credits",
                        color = ElectricBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (pkg.badge != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPopular) NeonViolet else TitonSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPopular) NeonMagenta else TitonBorder)
                    ) {
                        Text(
                            text = pkg.badge,
                            color = if (isPopular) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Price Details (HTG and USD)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${pkg.priceHtg} HTG",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(~$${pkg.priceUsd} USD)",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Estimations
            val videos5s = pkg.credits / 10
            val videos10s = pkg.credits / 20
            Text(
                text = "✓ Up to $videos5s standard (5s) or $videos10s extended (10s) AI videos",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = "✓ 4K Ultra HD upscaling & prompt enhancer included",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = "✓ Instant NatCash Haiti confirmation upon admin check",
                color = NatCashGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buy Credits with NatCash Button
            Button(
                onClick = onBuyWithNatcash,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPopular) ElectricBlue else NatCashGreen,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("buy_package_button_${pkg.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buy Credits via NatCash Haiti",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun TransactionRow(tx: TransactionEntity) {
    val dateStr = remember(tx.createdAt) {
        val sdf = SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault())
        sdf.format(Date(tx.createdAt))
    }

    val isPositive = tx.amount > 0
    val amountColor = if (isPositive) StatusSuccess else StatusError

    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$dateStr • Balance after: ${tx.balanceAfter} credits",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Text(
                text = if (isPositive) "+${tx.amount}" else "${tx.amount}",
                color = amountColor,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}
