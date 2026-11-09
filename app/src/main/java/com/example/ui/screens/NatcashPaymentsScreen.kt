package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NatcashPaymentEntity
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TitonViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NatcashPaymentsScreen(
    viewModel: TitonViewModel
) {
    val context = LocalContext.current
    val userPayments by viewModel.userPayments.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val merchantConfig by viewModel.merchantConfig.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val pendingCount = remember(userPayments) { userPayments.count { it.status == "PENDING" } }
    val approvedCount = remember(userPayments) { userPayments.count { it.status == "APPROVED" } }
    val rejectedCount = remember(userPayments) { userPayments.count { it.status == "REJECTED" } }

    val displayedPayments = remember(userPayments, selectedStatusFilter) {
        when (selectedStatusFilter) {
            "PENDING" -> userPayments.filter { it.status == "PENDING" }
            "APPROVED" -> userPayments.filter { it.status == "APPROVED" }
            "REJECTED" -> userPayments.filter { it.status == "REJECTED" }
            else -> userPayments
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TitonDarkVoid)
            .padding(horizontal = 16.dp)
            .testTag("payments_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NatCash Payments",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Track payment verification and credit releases in HTG",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { viewModel.navigateTo(AppScreen.PRICING) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NatCashGreen,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("buy_credits_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Buy Credits", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Live Merchant Credentials Card
        item {
            Surface(
                color = TitonSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NatCashGreen.copy(alpha = 0.4f)),
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(NatCashGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Official NatCash Merchant",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = merchantConfig.merchantName,
                                    color = NatCashGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TitonSurfaceVariant
                        ) {
                            Text(
                                text = "Dial ${merchantConfig.ussdCode}",
                                color = ElectricBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1-Tap Copy Merchant Number
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TitonDarkVoid,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Merchant Phone", merchantConfig.merchantPhone))
                                Toast.makeText(context, "Copied NatCash Merchant Number!", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Merchant Number: ",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = merchantConfig.merchantPhone,
                                    color = ElectricBlue,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Copy",
                                    color = ElectricBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Transfer the exact HTG amount via NatCash, then submit your transaction reference. Credits release automatically once verified by admin.",
                        color = TextMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Status Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedStatusFilter == "ALL",
                        onClick = { selectedStatusFilter = "ALL" },
                        label = { Text("All (${userPayments.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.Black,
                            containerColor = TitonSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedStatusFilter == "ALL",
                            borderColor = if (selectedStatusFilter == "ALL") ElectricBlue else TitonBorder
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = selectedStatusFilter == "PENDING",
                        onClick = { selectedStatusFilter = "PENDING" },
                        label = { Text("Pending ($pendingCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusPending,
                            selectedLabelColor = Color.Black,
                            containerColor = TitonSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedStatusFilter == "PENDING",
                            borderColor = if (selectedStatusFilter == "PENDING") StatusPending else TitonBorder
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = selectedStatusFilter == "APPROVED",
                        onClick = { selectedStatusFilter = "APPROVED" },
                        label = { Text("Approved ($approvedCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusSuccess,
                            selectedLabelColor = Color.Black,
                            containerColor = TitonSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedStatusFilter == "APPROVED",
                            borderColor = if (selectedStatusFilter == "APPROVED") StatusSuccess else TitonBorder
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = selectedStatusFilter == "REJECTED",
                        onClick = { selectedStatusFilter = "REJECTED" },
                        label = { Text("Rejected ($rejectedCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusError,
                            selectedLabelColor = Color.White,
                            containerColor = TitonSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedStatusFilter == "REJECTED",
                            borderColor = if (selectedStatusFilter == "REJECTED") StatusError else TitonBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Empty state
        if (displayedPayments.isEmpty()) {
            item {
                Surface(
                    color = TitonSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (userPayments.isEmpty()) "No NatCash payments submitted yet" else "No $selectedStatusFilter payments found",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Purchase a Starter, Pro, or Premium credit package to create videos.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // List of payments
        items(displayedPayments) { payment ->
            PaymentStatusCard(payment = payment)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun PaymentStatusCard(
    payment: NatcashPaymentEntity
) {
    val context = LocalContext.current
    val submitDateStr = remember(payment.createdAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(payment.createdAt))
    }

    val verifiedDateStr = remember(payment.reviewedAt) {
        payment.reviewedAt?.let {
            val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
            sdf.format(Date(it))
        }
    }

    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (payment.status) {
                "APPROVED" -> StatusSuccess.copy(alpha = 0.5f)
                "REJECTED" -> StatusError.copy(alpha = 0.5f)
                else -> TitonBorder
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Package and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = payment.packageName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+${payment.creditsAmount} AI Video Credits",
                        color = ElectricBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                StatusPill(status = payment.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transaction Details Box
            Surface(
                color = TitonSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Exact HTG Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Amount Paid in HTG:", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "${payment.amountHtg} HTG",
                            color = NatCashGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // NatCash Transaction Reference with 1-tap Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Transaction Ref:", color = TextMuted, fontSize = 11.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Transaction Ref", payment.transactionReference))
                                Toast.makeText(context, "Copied Transaction Reference!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = payment.transactionReference,
                                color = ElectricBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = ElectricBlue,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Sender Phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Sender Phone:", color = TextMuted, fontSize = 11.sp)
                        Text(text = payment.senderPhone, color = TextSecondary, fontSize = 11.sp)
                    }

                    if (!payment.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Your Note:", color = TextMuted, fontSize = 10.sp)
                            Text(text = payment.note, color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    // Admin Review Notes
                    if (!payment.adminNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (payment.status == "APPROVED") StatusSuccess.copy(alpha = 0.1f) else StatusError.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Admin Note: ${payment.adminNotes}",
                                color = if (payment.status == "APPROVED") StatusSuccess else StatusError,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Timestamps & Credit Release confirmation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Submitted: $submitDateStr",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                when (payment.status) {
                    "APPROVED" -> {
                        Text(
                            text = "✓ Verified • ${payment.creditsAmount} Credits Credited",
                            color = StatusSuccess,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    "PENDING" -> {
                        Text(
                            text = "⏳ Awaiting Admin Verification",
                            color = StatusPending,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    "REJECTED" -> {
                        Text(
                            text = "✕ Unverified / Rejected",
                            color = StatusError,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
