package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.CapUsageSummary
import com.example.model.PaymentMode
import com.example.model.PurchaseLog
import com.example.model.SpendCategory
import com.example.model.formatCurrency
import com.example.model.formatPercent
import com.example.model.formatTimestampReadable
import com.example.ui.components.CapProgressBar
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.CardEmeraldEnd
import com.example.ui.theme.CardEmeraldStart
import com.example.ui.theme.JetBrainsMono
import com.example.viewmodel.ActivitySummaryState

@Composable
fun ActivitySavingsScreen(
    summary: ActivitySummaryState,
    onDeleteTransaction: (PurchaseLog) -> Unit,
    onNavigateToAdvisor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Realized Savings Card
            item(key = "savings_hero") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(listOf(CardEmeraldStart, CardEmeraldEnd))
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Savings,
                                    contentDescription = "Total Savings",
                                    tint = Color(0xFFFDE68A),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SAVINGS THIS CYCLE (${summary.currentCycleKey})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Text(
                            text = formatCurrency(summary.totalSavedThisCycle, summary.currencySymbol),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = JetBrainsMono,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.testTag("total_savings_value")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "TRACKED SPEND",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.72f)
                                    )
                                )
                                Text(
                                    text = formatCurrency(summary.totalSpentThisCycle, summary.currencySymbol),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        color = Color.White
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "BLENDED RETURN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.72f)
                                    )
                                )
                                Text(
                                    text = formatPercent(summary.averageReturnPercent),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        color = Color(0xFFA7F3D0),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Monthly Benefit Cap Tracker
            if (summary.capSummaries.isNotEmpty()) {
                item(key = "caps_header") {
                    Column {
                        Text(
                            text = "Monthly Benefit Caps (${summary.currentCycleKey})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Live remaining caps automatically factor into Advisor card rankings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                itemsIndexed(
                    items = summary.capSummaries,
                    key = { index, cap -> "${cap.cardId}_${cap.ruleId}_$index" }
                ) { _, cap ->
                    CapTrackerCard(cap = cap)
                }
            }

            // Logged Purchases Section
            item(key = "logs_header") {
                Text(
                    text = "Logged Payments & Benefits (${summary.transactions.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (summary.transactions.isEmpty()) {
                item(key = "empty_activity") {
                    EmptyStateCard(
                        icon = Icons.Filled.ReceiptLong,
                        title = "No Logged Purchases Yet",
                        subtitle = "Search a merchant in Advisor and tap 'Select This Card for Payment' to track your savings and monthly cap usage.",
                        actionLabel = "Find Best Card",
                        onAction = onNavigateToAdvisor
                    )
                }
            } else {
                items(
                    items = summary.transactions,
                    key = { it.id }
                ) { tx ->
                    PurchaseLogRow(
                        tx = tx,
                        onDelete = { onDeleteTransaction(tx) }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CapTrackerCard(cap: CapUsageSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${cap.cardTitle} •• ${cap.cardLastFour}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = cap.ruleTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            CapProgressBar(
                usedAmount = cap.usedAmount,
                monthlyCap = cap.monthlyCap,
                currencySymbol = cap.currencySymbol
            )
        }
    }
}

@Composable
private fun PurchaseLogRow(
    tx: PurchaseLog,
    onDelete: () -> Unit
) {
    val displayTimeMs = tx.createdAt?.toDate()?.time ?: 0L

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("purchase_log_${tx.id}"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = tx.merchantName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (tx.sampleData) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "SAMPLE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Text(
                    text = tx.cardDisplayName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                )

                if (tx.itemNote.isNotBlank()) {
                    Text(
                        text = tx.itemNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${SpendCategory.fromRaw(tx.category).displayName} • ${PaymentMode.fromRaw(tx.paymentMode).shortLabel} • ${formatTimestampReadable(displayTimeMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (tx.matchedRuleTitle.isNotBlank()) {
                    Text(
                        text = "Offer: ${tx.matchedRuleTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${formatCurrency(tx.expectedBenefitAmount, tx.currencySymbol)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "on ${formatCurrency(tx.amount, tx.currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = formatPercent(tx.effectiveReturnPercent),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_tx_${tx.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete logged purchase",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
