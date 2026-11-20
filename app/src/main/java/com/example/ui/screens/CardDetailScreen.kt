package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BankCardCatalog
import com.example.model.BenefitPeriod
import com.example.model.CardBenefitRule
import com.example.model.CreditCard
import com.example.model.MilestoneRewardType
import com.example.model.RewardType
import com.example.model.RuleType
import com.example.model.SpendCategory
import com.example.model.formatCurrency
import com.example.model.formatPercent
import com.example.model.isSafeWebUri
import com.example.ui.components.MetallicCreditCardView
import com.example.ui.theme.JetBrainsMono
import com.example.viewmodel.GlobalCatalogUiState

@Composable
fun CardDetailScreen(
    card: CreditCard,
    catalogState: GlobalCatalogUiState = GlobalCatalogUiState(),
    onBack: () -> Unit,
    onUpdateCard: (CreditCard) -> Unit,
    onDeleteCard: () -> Unit,
    onSaveRule: (CardBenefitRule) -> Unit,
    onDeleteRule: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle system back press to return cleanly to My Cards list
    BackHandler(onBack = onBack)

    val uriHandler = LocalUriHandler.current
    val bankEntry = remember(card.issuer, catalogState.allBanks) {
        BankCardCatalog.findBank(card.issuer, catalogState.allBanks)
    }

    var showEditCardDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<CardBenefitRule?>(null) }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Actions Row
            item(key = "detail_top_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("card_detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to My Cards"
                            )
                        }
                        Text(
                            text = "Card Details",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Row {
                        IconButton(
                            onClick = { showEditCardDialog = true },
                            modifier = Modifier.testTag("edit_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit card details"
                            )
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("delete_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete card",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Full Interactive 3D Metallic Card Hero
            item(key = "metallic_hero") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetallicCreditCardView(
                        card = card,
                        enableInteractive3D = true
                    )
                    Text(
                        text = "Interactive 3D Card • Drag to tilt with dynamic sheen or tap to flip front/back",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Actions: Add Benefit Rule + Direct Official Bank Website Link
            item(key = "card_actions_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddRuleDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_rule_primary_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Benefit Rule", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    if (bankEntry != null && isSafeWebUri(bankEntry.officialWebsiteUrl)) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    uriHandler.openUri(bankEntry.officialWebsiteUrl)
                                } catch (_: Exception) {
                                    // Ignore if no external browser
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("open_official_bank_site_button"),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "Open ${bankEntry.officialDomain}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Filled.OpenInNew,
                                contentDescription = "Open official bank site",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Card Features Summary & Billing Stats
            item(key = "card_overview") {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Core Features & Reward Profile",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (card.aiSummary.isNotBlank()) {
                            Text(
                                text = card.aiSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricPill("BASE RATE", formatPercent(card.baseRewardRatePercent))
                            MetricPill("REWARD TYPE", RewardType.fromRaw(card.rewardType).displayName)
                            MetricPill("BILL / DUE", "Day ${card.billingDay} / ${card.dueDay}")
                        }
                    }
                }
            }

            // Benefit Rules Header + Add Override CTA
            item(key = "rules_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Benefits, Offers & Conditions (${card.rules.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Milestones, fuel waivers, dining coupons & merchant perks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { showAddRuleDialog = true },
                        modifier = Modifier.testTag("add_rule_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add custom rule",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Rule")
                    }
                }
            }

            if (card.rules.isEmpty()) {
                item(key = "no_rules") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Text(
                            text = "No benefit or offer rules added yet. Tap 'Add Benefit Rule' above to enter milestone bonuses, fuel waivers, dining offers with coupon codes, or merchant rewards.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = card.rules,
                    key = { index, rule -> "${rule.ruleId.ifBlank { rule.title }}_$index" }
                ) { _, rule ->
                    RuleDetailCard(
                        rule = rule,
                        currencySymbol = card.currencySymbol,
                        onEdit = { editingRule = rule },
                        onDelete = { onDeleteRule(rule.ruleId) }
                    )
                }
            }

            item(key = "bottom_space") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showEditCardDialog) {
        AddOrEditCardDialog(
            existingCard = card,
            catalogState = catalogState,
            onDismiss = { showEditCardDialog = false },
            onSave = { updated ->
                onUpdateCard(updated)
                showEditCardDialog = false
            }
        )
    }

    if (showAddRuleDialog) {
        EditRuleDialog(
            currencySymbol = card.currencySymbol,
            existingRule = null,
            onDismiss = { showAddRuleDialog = false },
            onSave = { rule ->
                onSaveRule(rule)
                showAddRuleDialog = false
            }
        )
    }

    val ruleBeingEdited = editingRule
    if (ruleBeingEdited != null) {
        EditRuleDialog(
            currencySymbol = card.currencySymbol,
            existingRule = ruleBeingEdited,
            onDismiss = { editingRule = null },
            onSave = { rule ->
                onSaveRule(rule)
                editingRule = null
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove ${card.displayTitle}?") },
            text = {
                Text("This removes the card and its saved benefit rules from your wallet.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteCard()
                        onBack()
                    },
                    modifier = Modifier.testTag("confirm_delete_card_button")
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MetricPill(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleDetailCard(
    rule: CardBenefitRule,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val ruleTypeEnum = RuleType.fromRaw(rule.ruleType)
    val period = BenefitPeriod.fromRaw(rule.benefitPeriod)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = ruleTypeEnum.displayName,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = SpendCategory.fromRaw(rule.category).displayName,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = period.displayName,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rule.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit rule",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete rule",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Context-specific structured metrics
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (ruleTypeEnum) {
                    RuleType.MILESTONE, RuleType.WELCOME -> {
                        if (rule.minSpend > 0.0) {
                            MetricPill(
                                label = "${period.displayName.uppercase()} TARGET",
                                value = formatCurrency(rule.minSpend, currencySymbol)
                            )
                        }
                        if (rule.milestoneRewardType.isNotBlank()) {
                            MetricPill(
                                label = "REWARD TYPE",
                                value = MilestoneRewardType.fromRaw(rule.milestoneRewardType).displayName
                            )
                        }
                        if (rule.flatBenefitAmount > 0.0) {
                            MetricPill(
                                label = "EST. VALUE",
                                value = formatCurrency(rule.flatBenefitAmount, currencySymbol)
                            )
                        }
                    }

                    RuleType.FUEL -> {
                        if (rule.benefitPercent > 0.0) {
                            MetricPill("WAIVER", formatPercent(rule.benefitPercent))
                        }
                        if (rule.minSpend > 0.0 || rule.maxTxnAmount > 0.0) {
                            val rangeText = when {
                                rule.minSpend > 0.0 && rule.maxTxnAmount > 0.0 ->
                                    "${formatCurrency(rule.minSpend, currencySymbol)}–${formatCurrency(rule.maxTxnAmount, currencySymbol)}"
                                rule.minSpend > 0.0 -> "Min ${formatCurrency(rule.minSpend, currencySymbol)}"
                                else -> "Up to ${formatCurrency(rule.maxTxnAmount, currencySymbol)}"
                            }
                            MetricPill("FUEL TXN RANGE", rangeText)
                        }
                        MetricPill(
                            label = "MAX WAIVER",
                            value = if (rule.monthlyCapAmount > 0.0) {
                                "${formatCurrency(rule.monthlyCapAmount, currencySymbol)} ${period.shortLabel}"
                            } else {
                                "Uncapped"
                            }
                        )
                    }

                    RuleType.LOUNGE -> {
                        if (rule.loungeVisitsCount > 0) {
                            MetricPill("FREE VISITS", "${rule.loungeVisitsCount} ${period.shortLabel}")
                        }
                        if (rule.minSpend > 0.0) {
                            MetricPill("PRIOR SPEND", formatCurrency(rule.minSpend, currencySymbol))
                        }
                        if (rule.flatBenefitAmount > 0.0) {
                            MetricPill("VALUE / VISIT", formatCurrency(rule.flatBenefitAmount, currencySymbol))
                        }
                    }

                    else -> {
                        if (rule.benefitPercent > 0.0) {
                            MetricPill("RATE", formatPercent(rule.benefitPercent))
                        }
                        if (rule.flatBenefitAmount > 0.0) {
                            MetricPill("FLAT VALUE", formatCurrency(rule.flatBenefitAmount, currencySymbol))
                        }
                        if (rule.minSpend > 0.0) {
                            MetricPill("MIN TXN", formatCurrency(rule.minSpend, currencySymbol))
                        }
                        MetricPill(
                            label = "CAP (${period.displayName.uppercase()})",
                            value = if (rule.monthlyCapAmount > 0.0) {
                                formatCurrency(rule.monthlyCapAmount, currencySymbol)
                            } else {
                                "Uncapped"
                            }
                        )
                    }
                }
            }

            if (rule.milestoneRewardDetail.isNotBlank()) {
                Text(
                    text = "Reward: ${rule.milestoneRewardDetail}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (rule.couponCode.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ConfirmationNumber,
                            contentDescription = "Coupon code",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COUPON CODE: ${rule.couponCode}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            if (rule.merchantKeywords.isNotEmpty()) {
                Text(
                    text = "Keywords: ${rule.merchantKeywords.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (rule.conditionsText.isNotBlank()) {
                Text(
                    text = "Conditions: ${rule.conditionsText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (rule.excludedCategories.isNotBlank()) {
                Text(
                    text = "Exclusions: ${rule.excludedCategories}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = "Channel: ${rule.promoOrChannelReq} • Validity: ${rule.validityWindow}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
