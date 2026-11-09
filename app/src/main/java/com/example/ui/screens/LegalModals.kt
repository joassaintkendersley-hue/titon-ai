package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun LegalModal(
    type: String, // "ABOUT", "HELP", "PRIVACY", "TERMS"
    onDismiss: () -> Unit
) {
    val (title, content) = when (type) {
        "ABOUT" -> Pair(
            "About TITON AI",
            """
            TITON AI is a state-of-the-art AI video generation platform inspired by Seedance neural architecture.

            Our Mission:
            "Turn Your Imagination Into Videos."

            We empower directors, content creators, marketers, and visual artists to produce cinema-grade AI videos directly from simple natural language prompts and reference images.

            Key Pillars:
            • Seedance Neural Synthesis: High-framerate physics simulation and photorealistic fluid coherence.
            • Multi-Format Production: Native rendering in 16:9 widescreen, 9:16 vertical reels, and 1:1 square compositions.
            • Fair Credit Economics: 50 Free Welcome Credits for every new creator, with automatic refunds if any generation fails.
            • Localized NatCash Haiti Gateway: Full payment flexibility allowing creators in Haiti to purchase packages in Haitian Gourdes (HTG) with zero international banking barriers.

            Official Brand:
            The official platform name is TITON AI. All rights reserved.
            """.trimIndent()
        )
        "HELP" -> Pair(
            "Help Center & FAQ",
            """
            Frequently Asked Questions:

            1. How many credits do videos cost?
            • 5-Second Video: 10 Credits
            • 10-Second Video: 20 Credits
            • 15-Second Video: 30 Credits

            2. How does NatCash Haiti payment work?
            • Select a Starter, Pro, or Premium package.
            • Dial *202# or open the NatCash app on your phone.
            • Send the exact amount in HTG to Merchant Number: +509 34 56 7890 (TITON AI INC.).
            • Enter the SMS transaction reference in the app.
            • Once verified by an administrator, credits are automatically added to your balance.

            3. What happens if video generation fails?
            Credits are never lost! If a generation job encounters an error or network timeout, the deducted credits are instantly refunded back to your account.

            4. How do I download generated videos?
            Tap on any completed video in your library or the studio to open the cinematic preview player, then tap 'Download' to save the MP4 directly to your device.
            """.trimIndent()
        )
        "PRIVACY" -> Pair(
            "Privacy Policy",
            """
            TITON AI respects your privacy and is committed to protecting your creative assets:

            • Data Collection: We store your account credentials, generation prompt history, and transaction references securely.
            • NatCash Security: We never request, capture, or store customer NatCash PINs. Only transaction reference numbers and sender phone numbers are logged for administrative payment verification.
            • Creative Ownership: You retain 100% full commercial rights and intellectual property over videos generated using your account credits.
            • API Integration: Provider API credentials entered in settings are stored locally on your device or secure backend.
            """.trimIndent()
        )
        else -> Pair(
            "Terms of Service",
            """
            TITON AI Terms of Service:

            1. Acceptance of Terms: By creating an account or purchasing credits on TITON AI, you agree to comply with our platform guidelines.
            2. Prohibited Content: Users may not generate illegal, defamatory, violent, or infringing visual media.
            3. Credit Validity: Purchased credits do not expire and can be used across all supported video generation engines.
            4. Brand Protection: The official platform name is strictly TITON AI.
            """.trimIndent()
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = TitonDarkVoid,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TitonBorderGlow),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Text(
                            text = content,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
