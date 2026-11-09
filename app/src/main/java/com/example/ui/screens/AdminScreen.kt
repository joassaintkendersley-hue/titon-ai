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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TitonViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: TitonViewModel
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allPackages by viewModel.allPackages.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val merchantConfig by viewModel.merchantConfig.collectAsState()

    val totalUsersCount by viewModel.userCount.collectAsState()
    val totalVideosCount by viewModel.totalVideosCount.collectAsState()
    val totalCreditsSold by viewModel.totalCreditsSold.collectAsState()
    val pendingPaymentsCount by viewModel.pendingPaymentsCount.collectAsState()
    val approvedPaymentsCount by viewModel.approvedPaymentsCount.collectAsState()
    val rejectedPaymentsCount by viewModel.rejectedPaymentsCount.collectAsState()
    val totalRevenueHtg by viewModel.totalRevenueHtg.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    var paymentFilterState by remember { mutableStateOf("PENDING") }
    val tabTitles = listOf(
        "Revenue & Stats",
        "Verify NatCash",
        "Merchant Config",
        "Packages",
        "Users",
        "AI Providers",
        "Ledger"
    )

    // Verification Dialog States
    var approvingPayment by remember { mutableStateOf<NatcashPaymentEntity?>(null) }
    var approvalNote by remember { mutableStateOf("Verified via NatCash merchant portal") }

    var rejectingPayment by remember { mutableStateOf<NatcashPaymentEntity?>(null) }
    var rejectionReason by remember { mutableStateOf("Transaction reference not found in NatCash statement") }

    // Security Gate: Check if user is admin
    if (currentUser?.role != "ADMIN") {
        Surface(
            color = TitonDarkVoid,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = StatusError,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Administrator Access Required",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This area is restricted to TITON AI administrators for NatCash verification, merchant configuration, and revenue auditing.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.switchAccount(1L) }, // Switch to Admin User (id=1)
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.testTag("switch_to_admin_button")
                ) {
                    Text("Switch to Admin Account", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TitonDarkVoid)
            .padding(horizontal = 16.dp)
            .testTag("admin_screen"),
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
                        text = "Admin Dashboard",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "TITON AI Control, NatCash Verification & HTG Revenue",
                        color = ElectricBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonViolet.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple)
                ) {
                    Text(
                        text = "ADMIN ACTIVE",
                        color = NeonMagenta,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Metrics Grid (KPI Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Top Verified Revenue Card in HTG
                Surface(
                    color = TitonSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NatCashGreen.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = NatCashGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VERIFIED REVENUE (HTG)",
                                    color = NatCashGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalRevenueHtg HTG",
                                color = TextPrimary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "From $approvedPaymentsCount verified NatCash payments",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NatCashGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = NatCashGreen,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Pending Verification",
                        value = "$pendingPaymentsCount",
                        icon = Icons.Default.HourglassTop,
                        color = StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Approved Payments",
                        value = "$approvedPaymentsCount",
                        icon = Icons.Default.CheckCircle,
                        color = StatusSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Credits Sold",
                        value = "$totalCreditsSold",
                        icon = Icons.Default.Bolt,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Users",
                        value = "$totalUsersCount",
                        icon = Icons.Default.People,
                        color = NeonPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tab Navigation
        item {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = TitonDarkVoid,
                contentColor = ElectricBlue,
                edgePadding = 0.dp,
                divider = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            val badge = if (index == 1 && pendingPaymentsCount > 0) " ($pendingPaymentsCount)" else ""
                            Text(
                                text = "$title$badge",
                                color = if (activeTab == index) ElectricBlue else TextMuted,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- TAB 0: REVENUE & ANALYTICS DASHBOARD ---
        if (activeTab == 0) {
            item {
                Surface(
                    color = TitonSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = NatCashGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Zero Simulation Revenue Guarantee",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Revenue in HTG reflects strictly verified payments received via NatCash and approved by an administrator. Unverified, pending, or rejected transfers do not add to revenue totals or grant user credits.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Package Sales Breakdown in HTG
            item {
                Text(
                    text = "NatCash Package Revenue Breakdown",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val approvedPayments = allPayments.filter { it.status == "APPROVED" }
            val packagesBreakdown = allPackages.map { pkg ->
                val matching = approvedPayments.filter { it.packageId == pkg.id }
                val count = matching.size
                val htgRevenue = matching.sumOf { it.amountHtg }
                val creditsIssued = matching.sumOf { it.creditsAmount }
                Triple(pkg, count, Pair(htgRevenue, creditsIssued))
            }

            items(packagesBreakdown) { (pkg, count, totals) ->
                Surface(
                    color = TitonSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = pkg.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${pkg.credits} credits package @ ${pkg.priceHtg} HTG",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$count purchases confirmed",
                                color = ElectricBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${totals.first} HTG",
                                color = NatCashGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "+${totals.second} credits",
                                color = StatusSuccess,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Recent Verified Revenue Stream
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Recent Verified Revenue Streams (${approvedPayments.size})",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (approvedPayments.isEmpty()) {
                item {
                    Surface(
                        color = TitonSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No verified NatCash payments recorded yet.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(approvedPayments.take(8)) { p ->
                    PaymentStatusCard(payment = p)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // --- TAB 1: NATCASH PAYMENT VERIFICATION ---
        if (activeTab == 1) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = paymentFilterState == "PENDING",
                            onClick = { paymentFilterState = "PENDING" },
                            label = { Text("Pending (${allPayments.count { it.status == "PENDING" }})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusPending,
                                selectedLabelColor = Color.Black,
                                containerColor = TitonSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = paymentFilterState == "APPROVED",
                            onClick = { paymentFilterState = "APPROVED" },
                            label = { Text("Approved (${allPayments.count { it.status == "APPROVED" }})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusSuccess,
                                selectedLabelColor = Color.Black,
                                containerColor = TitonSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = paymentFilterState == "REJECTED",
                            onClick = { paymentFilterState = "REJECTED" },
                            label = { Text("Rejected (${allPayments.count { it.status == "REJECTED" }})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusError,
                                selectedLabelColor = Color.White,
                                containerColor = TitonSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = paymentFilterState == "ALL",
                            onClick = { paymentFilterState = "ALL" },
                            label = { Text("All (${allPayments.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue,
                                selectedLabelColor = Color.Black,
                                containerColor = TitonSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            val filteredList = when (paymentFilterState) {
                "PENDING" -> allPayments.filter { it.status == "PENDING" }
                "APPROVED" -> allPayments.filter { it.status == "APPROVED" }
                "REJECTED" -> allPayments.filter { it.status == "REJECTED" }
                else -> allPayments
            }

            if (filteredList.isEmpty()) {
                item {
                    Surface(
                        color = TitonSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "No $paymentFilterState payments found in queue.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(filteredList) { payment ->
                    AdminPaymentVerificationCard(
                        payment = payment,
                        onApprove = { approvingPayment = payment },
                        onReject = { rejectingPayment = payment }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // --- TAB 2: CONFIGURABLE NATCASH MERCHANT ---
        if (activeTab == 2) {
            item {
                AdminMerchantConfigSection(
                    currentConfig = merchantConfig,
                    onSave = { phone, name, ussd, instr ->
                        viewModel.updateMerchantConfig(phone, name, ussd, instr)
                    }
                )
            }
        }

        // --- TAB 3: PACKAGES MANAGEMENT ---
        if (activeTab == 3) {
            items(allPackages) { pkg ->
                AdminPackageConfigCard(
                    pkg = pkg,
                    onSave = { updated -> viewModel.updatePackage(updated) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // --- TAB 4: USER MANAGEMENT ---
        if (activeTab == 4) {
            items(allUsers) { user ->
                AdminUserCard(
                    user = user,
                    onGrantCredits = { amount -> viewModel.grantCredits(user.id, amount, "Admin reward") },
                    onToggleRole = { viewModel.toggleUserAdminRole(user.id, user.role) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // --- TAB 5: AI PROVIDERS & API KEYS ---
        if (activeTab == 5) {
            item {
                Surface(
                    color = TitonSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Real AI Video Provider Credentials", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Save real provider API keys here (Seedance, Fal.ai, Replicate, or your custom video backend). Video generation connects to these endpoints.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            items(providers) { provider ->
                AdminProviderCard(
                    provider = provider,
                    effectiveKey = viewModel.getEffectiveApiKey(provider),
                    onSave = { updated -> viewModel.updateProvider(updated) },
                    onTest = { p -> viewModel.testProviderConnection(p) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // --- TAB 6: AUDIT LEDGER ---
        if (activeTab == 6) {
            items(allTransactions) { tx ->
                TransactionRow(tx = tx)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Modal: Approve Payment Confirmation
    approvingPayment?.let { payment ->
        AlertDialog(
            onDismissRequest = { approvingPayment = null },
            title = {
                Text(
                    text = "Approve NatCash Payment",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to verify this transaction? This will automatically add +${payment.creditsAmount} credits to ${payment.userEmail}.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Amount: ${payment.amountHtg} HTG • Ref: ${payment.transactionReference}",
                        color = NatCashGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = approvalNote,
                        onValueChange = { approvalNote = it },
                        label = { Text("Admin Verification Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = StatusSuccess,
                            unfocusedBorderColor = TitonBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approvePayment(payment.id, approvalNote)
                        approvingPayment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess, contentColor = Color.Black)
                ) {
                    Text("Confirm & Release Credits", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { approvingPayment = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TitonSurface
        )
    }

    // Modal: Reject Payment
    rejectingPayment?.let { payment ->
        AlertDialog(
            onDismissRequest = { rejectingPayment = null },
            title = {
                Text(
                    text = "Reject NatCash Payment",
                    color = StatusError,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Specify the reason for rejecting this payment request from ${payment.userEmail}:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Rejection Reason") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = StatusError,
                            unfocusedBorderColor = TitonBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectPayment(payment.id, rejectionReason)
                        rejectingPayment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError, contentColor = Color.White)
                ) {
                    Text("Reject Payment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingPayment = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TitonSurface
        )
    }
}

@Composable
fun AdminPaymentVerificationCard(
    payment: NatcashPaymentEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    val submitDateStr = remember(payment.createdAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(payment.createdAt))
    }

    val isPending = payment.status == "PENDING"

    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (payment.status) {
                "PENDING" -> StatusPending.copy(alpha = 0.8f)
                "APPROVED" -> StatusSuccess.copy(alpha = 0.5f)
                else -> StatusError.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${payment.packageName} (+${payment.creditsAmount} Credits)",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "User: ${payment.userEmail}",
                        color = ElectricBlue,
                        fontSize = 11.5.sp
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
                        Text(text = "Amount Claimed:", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "${payment.amountHtg} HTG",
                            color = NatCashGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // NatCash Reference with 1-tap Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "NatCash Ref:", color = TextMuted, fontSize = 11.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("NatCash Ref", payment.transactionReference))
                                Toast.makeText(context, "Copied NatCash Reference!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = payment.transactionReference,
                                color = ElectricBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
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

                    // Sender Phone with 1-tap Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sender Phone:", color = TextMuted, fontSize = 11.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Sender Phone", payment.senderPhone))
                                Toast.makeText(context, "Copied Sender Phone!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = payment.senderPhone,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    if (!payment.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Customer Note: ${payment.note}", color = TextMuted, fontSize = 10.5.sp)
                    }

                    if (!payment.adminNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Admin Note: ${payment.adminNotes}",
                            color = if (payment.status == "APPROVED") StatusSuccess else StatusError,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Submitted: $submitDateStr",
                color = TextMuted,
                fontSize = 10.sp
            )

            if (isPending) {
                Spacer(modifier = Modifier.height(10.dp))
                // Action Buttons for Verification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusError),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusSuccess,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1.8f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve & Add Credits", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMerchantConfigSection(
    currentConfig: MerchantConfigEntity,
    onSave: (phone: String, name: String, ussd: String, instructions: String) -> Unit
) {
    var phoneInput by remember(currentConfig) { mutableStateOf(currentConfig.merchantPhone) }
    var nameInput by remember(currentConfig) { mutableStateOf(currentConfig.merchantName) }
    var ussdInput by remember(currentConfig) { mutableStateOf(currentConfig.ussdCode) }
    var instrInput by remember(currentConfig) { mutableStateOf(currentConfig.instructions) }

    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NatCashGreen.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SettingsPhone,
                    contentDescription = null,
                    tint = NatCashGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "NatCash Merchant Configuration",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Configures the recipient number and instructions for Haiti customers",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("NatCash Merchant Phone Number") },
                placeholder = { Text("+509 3100-8888") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_merchant_phone_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NatCashGreen,
                    unfocusedBorderColor = TitonBorder
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Merchant / Business Account Name") },
                placeholder = { Text("TITON AI HAITI SAS") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NatCashGreen,
                    unfocusedBorderColor = TitonBorder
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ussdInput,
                onValueChange = { ussdInput = it },
                label = { Text("NatCash USSD Dial Code") },
                placeholder = { Text("*202#") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NatCashGreen,
                    unfocusedBorderColor = TitonBorder
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = instrInput,
                onValueChange = { instrInput = it },
                label = { Text("Customer Transfer Instructions") },
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NatCashGreen,
                    unfocusedBorderColor = TitonBorder
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Live Preview Box
            Surface(
                color = TitonSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "LIVE CUSTOMER PREVIEW:",
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Send HTG to: $phoneInput ($nameInput)",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dial code: $ussdInput • Instructions: $instrInput",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onSave(phoneInput, nameInput, ussdInput, instrInput) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NatCashGreen,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_merchant_config_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save NatCash Merchant Credentials", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(text = title, color = TextMuted, fontSize = 10.sp)
                Text(text = value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserEntity,
    onGrantCredits: (Int) -> Unit,
    onToggleRole: () -> Unit
) {
    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = user.username, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (user.role == "ADMIN") NeonViolet else TitonSurfaceVariant
                        ) {
                            Text(
                                text = user.role,
                                color = if (user.role == "ADMIN") Color.White else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(text = user.email, color = TextMuted, fontSize = 11.sp)
                }

                Text(
                    text = "${user.credits} Credits",
                    color = ElectricBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(50, 100, 500).forEach { amount ->
                        OutlinedButton(
                            onClick = { onGrantCredits(amount) },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("+$amount", color = ElectricBlue, fontSize = 10.sp)
                        }
                    }
                }

                TextButton(
                    onClick = onToggleRole,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (user.role == "ADMIN") "Revoke Admin" else "Make Admin",
                        color = NeonMagenta,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminPackageConfigCard(
    pkg: CreditPackageEntity,
    onSave: (CreditPackageEntity) -> Unit
) {
    var creditsInput by remember(pkg.credits) { mutableStateOf(pkg.credits.toString()) }
    var priceHtgInput by remember(pkg.priceHtg) { mutableStateOf(pkg.priceHtg.toString()) }
    var priceUsdInput by remember(pkg.priceUsd) { mutableStateOf(pkg.priceUsd.toString()) }

    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = pkg.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = creditsInput,
                    onValueChange = { creditsInput = it },
                    label = { Text("Credits", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = TitonBorder
                    )
                )

                OutlinedTextField(
                    value = priceHtgInput,
                    onValueChange = { priceHtgInput = it },
                    label = { Text("HTG Price", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = TitonBorder
                    )
                )

                OutlinedTextField(
                    value = priceUsdInput,
                    onValueChange = { priceUsdInput = it },
                    label = { Text("USD Price", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = TitonBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val c = creditsInput.toIntOrNull() ?: pkg.credits
                    val htg = priceHtgInput.toIntOrNull() ?: pkg.priceHtg
                    val usd = priceUsdInput.toDoubleOrNull() ?: pkg.priceUsd
                    onSave(pkg.copy(credits = c, priceHtg = htg, priceUsd = usd))
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.Black),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save Package", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminProviderCard(
    provider: ProviderConfigEntity,
    effectiveKey: String,
    onSave: (ProviderConfigEntity) -> Unit,
    onTest: (ProviderConfigEntity) -> Unit
) {
    var apiKeyInput by remember(provider.apiKey) { mutableStateOf(provider.apiKey) }
    var endpointInput by remember(provider.apiEndpoint) { mutableStateOf(provider.apiEndpoint) }
    var modelInput by remember(provider.defaultModel) { mutableStateOf(provider.defaultModel) }
    var isKeyVisible by remember { mutableStateOf(false) }

    val hasKey = apiKeyInput.isNotBlank() || effectiveKey.isNotBlank()

    Surface(
        color = TitonSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = provider.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Engine ID: ${provider.id}", color = TextMuted, fontSize = 10.sp)
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (hasKey) StatusSuccess.copy(alpha = 0.2f) else StatusPending.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (hasKey) "KEY ACTIVE" else "NO KEY SET",
                        color = if (hasKey) StatusSuccess else StatusPending,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("API Key / Bearer Token", fontSize = 11.sp) },
                placeholder = {
                    Text(
                        if (effectiveKey.isNotBlank() && apiKeyInput.isBlank()) "Active via Secrets (••••••••${effectiveKey.takeLast(4)})"
                        else "Enter secret API Key",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                },
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

            Spacer(modifier = Modifier.height(6.dp))

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

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = modelInput,
                onValueChange = { modelInput = it },
                label = { Text("Model Name", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = TitonBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val current = provider.copy(
                            apiKey = apiKeyInput.trim().ifBlank { effectiveKey },
                            apiEndpoint = endpointInput.trim(),
                            defaultModel = modelInput.trim()
                        )
                        onTest(current)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onSave(provider.copy(apiKey = apiKeyInput.trim(), apiEndpoint = endpointInput.trim(), defaultModel = modelInput.trim()))
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.Black),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Config", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

