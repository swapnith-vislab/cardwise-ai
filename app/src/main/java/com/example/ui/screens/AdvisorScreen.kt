package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.BenefitRankingEngine
import com.example.domain.RankedCardRecommendation
import com.example.model.BenefitPeriod
import com.example.model.CardNetwork
import com.example.model.MilestoneRewardType
import com.example.model.PaymentMode
import com.example.model.RewardType
import com.example.model.RuleType
import com.example.model.SpendCategory
import com.example.model.formatCurrency
import com.example.model.formatPercent
import com.example.model.sanitizeDecimalInput
import com.example.ui.components.CapProgressBar
import com.example.ui.components.CategoryChipRow
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PaymentModeSelectorRow
import com.example.ui.theme.JetBrainsMono
import com.example.viewmodel.AdvisorUiState

private data class MerchantPreset(
    val label: String,
    val query: String,
    val category: SpendCategory,
    val mode: PaymentMode = PaymentMode.ONLINE
)

private val QUICK_MERCHANTS = listOf(
    MerchantPreset("Amazon", "Amazon", SpendCategory.SHOPPING, PaymentMode.ONLINE),
    MerchantPreset("Swiggy", "Swiggy", SpendCategory.DINING, PaymentMode.ONLINE),
    MerchantPreset("Electricity", "Electricity", SpendCategory.UTILITIES, PaymentMode.PORTAL),
    MerchantPreset("SmartBuy", "SmartBuy", SpendCategory.TRAVEL, PaymentMode.PORTAL),
    MerchantPreset("BigBasket", "BigBasket", SpendCategory.GROCERIES, PaymentMode.ONLINE),
    MerchantPreset("Shell Fuel", "Shell", SpendCategory.FUEL, PaymentMode.IN_STORE)
)

@Composable
fun AdvisorScreen(
    state: AdvisorUiState,
    onMerchantChange: (String) -> Unit,
    onPresetSelected: (String, SpendCategory, PaymentMode) -> Unit,
    onCategoryChange: (SpendCategory) -> Unit,
    onAmountChange: (String) -> Unit,
    onPaymentModeChange: (PaymentMode) -> Unit,
    onSelectCardForPayment: (RankedCardRecommendation, String, String, Double) -> Unit,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedForConfirm by remember { mutableStateOf<RankedCardRecommendation?>(null) }
    val trimmedLength = state.merchantQuery.trim().length

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
            // Search & Purchase Context Panel
            item(key = "search_panel") {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Best Card for Purchase",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "Enter more than 3 letters of a merchant or keyword to rank cards with matching offers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Merchant Search Field
                        OutlinedTextField(
                            value = state.merchantQuery,
                            onValueChange = onMerchantChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("merchant_search_input"),
                            label = { Text("Merchant / Keyword (4+ letters, e.g. Amazon, Swiggy)") },
                            supportingText = {
                                if (trimmedLength in 1 until BenefitRankingEngine.MIN_SEARCH_QUERY_LENGTH) {
                                    val remaining = BenefitRankingEngine.MIN_SEARCH_QUERY_LENGTH - trimmedLength
                                    Text(
                                        text = "Type $remaining more letter${if (remaining == 1) "" else "s"} to match card keywords",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search merchant"
                                )
                            },
                            trailingIcon = {
                                if (state.merchantQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onMerchantChange("") },
                                        modifier = Modifier.testTag("clear_merchant_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = "Clear search"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        // Quick Merchant Presets
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(QUICK_MERCHANTS) { preset ->
                                AssistChip(
                                    onClick = {
                                        onPresetSelected(preset.query, preset.category, preset.mode)
                                    },
                                    label = { Text(preset.label) },
                                    modifier = Modifier.testTag("preset_${preset.label.lowercase().replace(" ", "_")}")
                                )
                            }
                        }

                        // Offline SQLite Recent Searches (Room Cache)
                        if (state.recentSearches.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "RECENT SEARCHES (LOCAL CACHE)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(state.recentSearches, key = { it.merchantKey }) { recent ->
                                        AssistChip(
                                            onClick = {
                                                onPresetSelected(
                                                    recent.merchantLabel,
                                                    SpendCategory.fromRaw(recent.categoryName),
                                                    PaymentMode.fromRaw(recent.paymentModeName)
                                                )
                                            },
                                            label = { Text(recent.merchantLabel) }
                                        )
                                    }
                                }
                            }
                        }

                        // Amount + Category
                        OutlinedTextField(
                            value = state.amountInput,
                            onValueChange = onAmountChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("purchase_amount_input"),
                            label = { Text("Estimated Purchase Amount (optional)") },
                            placeholder = { Text("Leave blank to compare on 10,000 reference") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CATEGORY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            CategoryChipRow(
                                selected = state.selectedCategory,
                                onSelect = onCategoryChange
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "PAYMENT CHANNEL / MODE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PaymentModeSelectorRow(
                                selected = state.selectedPaymentMode,
                                onSelect = onPaymentModeChange
                            )
                        }
                    }
                }
            }

            // Results Section
            if (state.isLoading) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.errorMessage != null) {
                item(key = "error") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = state.errorMessage,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (!state.hasCardsInWallet) {
                item(key = "empty_wallet") {
                    EmptyStateCard(
                        icon = Icons.Filled.CreditCard,
                        title = "No Credit Cards in Your Wallet",
                        subtitle = "Add your credit cards in the My Cards tab to compare merchant offers, rewards, and monthly caps.",
                        actionLabel = "Go to My Cards",
                        onAction = onNavigateToWallet
                    )
                }
            } else if (state.isQueryTooShort) {
                item(key = "query_too_short") {
                    EmptyStateCard(
                        icon = Icons.Filled.Search,
                        title = "Enter More Than 3 Letters",
                        subtitle = "Type at least 4 letters (e.g. Amazon, Swiggy, Shell, Electricity) or tap a quick preset above to rank cards that match your keyword.",
                        modifier = Modifier.testTag("search_min_chars_notice")
                    )
                }
            } else if (state.recommendations.isEmpty()) {
                item(key = "no_keyword_matches") {
                    EmptyStateCard(
                        icon = Icons.Filled.SearchOff,
                        title = "No Cards Match \"${state.merchantQuery.trim()}\"",
                        subtitle = "None of your saved cards have a benefit rule with keywords matching \"${state.merchantQuery.trim()}\". Add a custom benefit rule in My Cards to include it in rankings.",
                        actionLabel = "Add Rule in My Cards",
                        onAction = onNavigateToWallet,
                        modifier = Modifier.testTag("no_keyword_match_empty_state")
                    )
                }
            } else {
                item(key = "results_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Matched Ranked Cards (${state.recommendations.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (state.amountInput.toDoubleOrNull()?.let { it > 0 } == true) {
                                "Net value for entered spend"
                            } else {
                                "Shown per 10,000 spend"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(
                    items = state.recommendations,
                    key = { it.card.id }
                ) { recommendation ->
                    RankedCardComparisonItem(
                        recommendation = recommendation,
                        onSelectForPayment = { selectedForConfirm = recommendation }
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Confirm & Log Purchase Dialog
    val activeSelection = selectedForConfirm
    if (activeSelection != null) {
        ConfirmPurchaseDialog(
            recommendation = activeSelection,
            initialMerchant = state.merchantQuery.ifBlank { state.selectedCategory.displayName },
            initialAmount = state.amountInput.ifBlank { activeSelection.evaluatedAmount.toLong().toString() },
            onDismiss = { selectedForConfirm = null },
            onConfirm = { merchant, note, amount ->
                onSelectCardForPayment(activeSelection, merchant, note, amount)
                selectedForConfirm = null
            }
        )
    }
}

@Composable
private fun RankedCardComparisonItem(
    recommendation: RankedCardRecommendation,
    onSelectForPayment: () -> Unit
) {
    val isBest = recommendation.rank == 1 && recommendation.estimatedBenefitAmount > 0.0
    var expanded by remember(recommendation.card.id, isBest) { mutableStateOf(isBest) }
    val card = recommendation.card
    val currency = card.currencySymbol
    val matchedRule = recommendation.matchedRule

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ranked_card_${card.id}"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isBest) 4.dp else 1.dp,
        border = BorderStroke(
            width = if (isBest) 2.dp else 1.dp,
            color = if (isBest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Rank Badge + Card Identity + Estimated Benefit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isBest) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isBest) {
                                    Icon(
                                        imageVector = Icons.Filled.WorkspacePremium,
                                        contentDescription = "Best Pick",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (isBest) "#1 BEST PICK" else "#${recommendation.rank}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isBest) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        if (matchedRule != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = RuleType.fromRaw(matchedRule.ruleType).displayName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = card.displayTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${CardNetwork.fromRaw(card.network).displayName} • ${card.maskedEnding}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMono,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Value Display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(recommendation.estimatedBenefitAmount, currency),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            color = if (recommendation.estimatedBenefitAmount > 0.0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                    Text(
                        text = "${formatPercent(recommendation.effectiveReturnPercent)} return",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    if (recommendation.estimatedPointsEarned > 0.0 && card.rewardType != RewardType.CASHBACK.name) {
                        Text(
                            text = "≈ ${recommendation.estimatedPointsEarned.toLong()} ${RewardType.fromRaw(card.rewardType).displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Headline Offer Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalOffer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = recommendation.headlineOffer,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        if (recommendation.matchedKeywords.isNotEmpty()) {
                            Text(
                                text = "Matched keyword: ${recommendation.matchedKeywords.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Coupon Code Badge (when matched rule has a promo / coupon code)
            if (recommendation.couponCode.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ConfirmationNumber,
                            contentDescription = "Coupon code",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "USE COUPON CODE: ${recommendation.couponCode}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Warnings (min spend, max txn, cap exhausted, EMI exclusion, portal requirement)
            if (recommendation.warnings.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    recommendation.warnings.forEach { warning ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "Condition notice",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = warning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // Remaining Benefit Cap Progress
            if (recommendation.monthlyCapAmount > 0.0) {
                CapProgressBar(
                    usedAmount = recommendation.usedCapInCycle,
                    monthlyCap = recommendation.monthlyCapAmount,
                    currencySymbol = currency
                )
            } else {
                Text(
                    text = "Uncapped benefit • No cycle limit",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Expandable Eligibility & Conditions Section
            TextButton(
                onClick = { expanded = !expanded },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier.testTag("toggle_conditions_${card.id}")
            ) {
                Text(
                    text = if (expanded) "Hide Conditions & Caps" else "Eligibility & Conditions",
                    style = MaterialTheme.typography.labelMedium
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse conditions" else "Expand conditions",
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ConditionDetailRow("Eligibility", recommendation.conditionsSummary)
                    if (matchedRule != null) {
                        val periodLabel = BenefitPeriod.fromRaw(matchedRule.benefitPeriod).displayName
                        if (matchedRule.milestoneRewardDetail.isNotBlank()) {
                            val rewardType = MilestoneRewardType.fromRaw(matchedRule.milestoneRewardType).displayName
                            ConditionDetailRow("Milestone Reward ($periodLabel)", "$rewardType — ${matchedRule.milestoneRewardDetail}")
                        }
                        if (matchedRule.ruleType == RuleType.FUEL.name && matchedRule.maxTxnAmount > 0.0) {
                            ConditionDetailRow(
                                "Eligible Fuel Range",
                                "${formatCurrency(matchedRule.minSpend, currency)} to ${formatCurrency(matchedRule.maxTxnAmount, currency)}"
                            )
                        }
                        if (matchedRule.loungeVisitsCount > 0) {
                            ConditionDetailRow("Complimentary Visits", "${matchedRule.loungeVisitsCount} ($periodLabel)")
                        }
                    }
                    if (recommendation.promoOrChannelReq.isNotBlank()) {
                        ConditionDetailRow("Channel / How to Avail", recommendation.promoOrChannelReq)
                    }
                    if (recommendation.excludedCategories.isNotBlank()) {
                        ConditionDetailRow("Exclusions", recommendation.excludedCategories)
                    }
                    ConditionDetailRow("Validity", recommendation.validityWindow)
                }
            }

            // Primary Action CTA
            if (isBest) {
                Button(
                    onClick = onSelectForPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("select_card_button_${card.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select This Card for Payment")
                }
            } else {
                OutlinedButton(
                    onClick = onSelectForPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("select_card_button_${card.id}"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Select This Card for Payment")
                }
            }
        }
    }
}

@Composable
private fun ConditionDetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ConfirmPurchaseDialog(
    recommendation: RankedCardRecommendation,
    initialMerchant: String,
    initialAmount: String,
    onDismiss: () -> Unit,
    onConfirm: (merchant: String, itemNote: String, amount: Double) -> Unit
) {
    var merchant by remember { mutableStateOf(initialMerchant) }
    var amountText by remember { mutableStateOf(initialAmount) }
    var note by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Card & Log Purchase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Paying with ${recommendation.card.displayTitle} (${recommendation.card.maskedEnding})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Logging this purchase updates your Activity savings and deducts from this card's monthly benefit cap.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_merchant_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = sanitizeDecimalInput(it) },
                    label = { Text("Purchase Amount (${recommendation.card.currencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_amount_input")
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Item / Order Note (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_note_input")
                )

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        validationError = "Please enter an amount greater than 0."
                    } else {
                        onConfirm(merchant.trim().ifBlank { "Purchase" }, note.trim(), amt)
                    }
                },
                modifier = Modifier.testTag("confirm_log_purchase_button")
            ) {
                Text("Log & Use Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
