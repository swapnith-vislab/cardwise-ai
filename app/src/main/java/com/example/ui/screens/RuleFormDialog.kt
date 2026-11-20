package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.BenefitRankingEngine
import com.example.model.BenefitPeriod
import com.example.model.CardBenefitRule
import com.example.model.MilestoneRewardType
import com.example.model.PaymentMode
import com.example.model.RuleType
import com.example.model.SpendCategory
import com.example.model.sanitizeDecimalInput
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditRuleDialog(
    currencySymbol: String,
    existingRule: CardBenefitRule? = null,
    onDismiss: () -> Unit,
    onSave: (CardBenefitRule) -> Unit
) {
    val isEditing = existingRule != null

    var ruleType by remember {
        mutableStateOf(RuleType.fromRaw(existingRule?.ruleType ?: RuleType.MERCHANT_OFFER.name))
    }
    var category by remember {
        mutableStateOf(SpendCategory.fromRaw(existingRule?.category ?: SpendCategory.SHOPPING.name))
    }
    var title by remember { mutableStateOf(existingRule?.title ?: "") }
    var merchantKeywordsText by remember {
        mutableStateOf(existingRule?.merchantKeywords?.joinToString(", ") ?: "")
    }
    var benefitPeriod by remember {
        mutableStateOf(BenefitPeriod.fromRaw(existingRule?.benefitPeriod ?: BenefitPeriod.MONTHLY.name))
    }
    var benefitPercentText by remember {
        mutableStateOf(
            existingRule?.benefitPercent?.takeIf { it > 0.0 }?.toString()
                ?: if (existingRule == null) "5.0" else ""
        )
    }
    var flatAmountText by remember {
        mutableStateOf(existingRule?.flatBenefitAmount?.takeIf { it > 0.0 }?.toLong()?.toString() ?: "")
    }
    var pointsPer100Text by remember {
        mutableStateOf(existingRule?.pointsPer100?.takeIf { it > 0.0 }?.toString() ?: "")
    }
    var minSpendText by remember {
        mutableStateOf(existingRule?.minSpend?.takeIf { it > 0.0 }?.toLong()?.toString() ?: "")
    }
    var maxTxnAmountText by remember {
        mutableStateOf(existingRule?.maxTxnAmount?.takeIf { it > 0.0 }?.toLong()?.toString() ?: "")
    }
    var capAmountText by remember {
        mutableStateOf(existingRule?.monthlyCapAmount?.takeIf { it > 0.0 }?.toLong()?.toString() ?: "")
    }
    var milestoneRewardType by remember {
        mutableStateOf(
            if (!existingRule?.milestoneRewardType.isNullOrBlank()) {
                MilestoneRewardType.fromRaw(existingRule!!.milestoneRewardType)
            } else {
                MilestoneRewardType.REWARD_POINTS
            }
        )
    }
    var milestoneRewardDetail by remember {
        mutableStateOf(existingRule?.milestoneRewardDetail ?: "")
    }
    var couponCode by remember {
        mutableStateOf(existingRule?.couponCode ?: "")
    }
    var loungeVisitsText by remember {
        mutableStateOf(existingRule?.loungeVisitsCount?.takeIf { it > 0 }?.toString() ?: "2")
    }
    var selectedMode by remember {
        val raw = existingRule?.paymentModes?.firstOrNull() ?: "ONLINE"
        mutableStateOf(if (raw == "ANY") "ANY" else PaymentMode.fromRaw(raw).name)
    }
    var excludedText by remember { mutableStateOf(existingRule?.excludedCategories ?: "") }
    var conditionsText by remember { mutableStateOf(existingRule?.conditionsText ?: "") }
    var validityWindow by remember { mutableStateOf(existingRule?.validityWindow ?: "Ongoing") }
    var promoReq by remember { mutableStateOf(existingRule?.promoOrChannelReq ?: "Direct checkout") }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun appendKeywordChip(keyword: String) {
        val current = merchantKeywordsText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toMutableList()
        if (current.none { it.equals(keyword, ignoreCase = true) }) {
            current.add(keyword.lowercase(Locale.ROOT))
            merchantKeywordsText = current.joinToString(", ")
        }
    }

    fun onRuleTypeChanged(newType: RuleType) {
        ruleType = newType
        errorText = null
        // Smart defaults based on newly chosen Rule Type
        when (newType) {
            RuleType.FUEL -> {
                category = SpendCategory.FUEL
                benefitPeriod = BenefitPeriod.MONTHLY
                if (benefitPercentText.isBlank() || benefitPercentText == "5.0") benefitPercentText = "1.0"
                if (minSpendText.isBlank()) minSpendText = "400"
                if (maxTxnAmountText.isBlank()) maxTxnAmountText = "4000"
                if (capAmountText.isBlank()) capAmountText = "250"
                selectedMode = PaymentMode.IN_STORE.name
                promoReq = "POS terminal at fuel stations"
            }

            RuleType.MILESTONE -> {
                category = SpendCategory.ALL
                benefitPeriod = BenefitPeriod.QUARTERLY
                selectedMode = "ANY"
                promoReq = "Claim via bank milestone portal"
            }

            RuleType.DINING -> {
                category = SpendCategory.DINING
                benefitPeriod = BenefitPeriod.PER_TXN
                selectedMode = "ANY"
                promoReq = "Dining app / restaurant bill pay"
            }

            RuleType.LOUNGE -> {
                category = SpendCategory.TRAVEL
                benefitPeriod = BenefitPeriod.QUARTERLY
                selectedMode = PaymentMode.IN_STORE.name
                promoReq = "Swipe card or DreamFolks QR at lounge"
            }

            RuleType.TRAVEL -> {
                category = SpendCategory.TRAVEL
                benefitPeriod = BenefitPeriod.MONTHLY
                selectedMode = PaymentMode.PORTAL.name
                promoReq = "Bank travel portal / airline checkout"
            }

            RuleType.WELCOME -> {
                category = SpendCategory.ALL
                benefitPeriod = BenefitPeriod.ONE_TIME
                selectedMode = "ANY"
                if (validityWindow == "Ongoing") validityWindow = "Within 90 days of card issue"
                promoReq = "Auto-credited after target spend"
            }

            RuleType.MERCHANT_OFFER -> {
                if (category == SpendCategory.FUEL) category = SpendCategory.SHOPPING
                benefitPeriod = BenefitPeriod.PER_TXN
                selectedMode = PaymentMode.ONLINE.name
                promoReq = "Direct checkout"
            }

            RuleType.CATEGORY_REWARD -> {
                benefitPeriod = BenefitPeriod.MONTHLY
                selectedMode = PaymentMode.ONLINE.name
                promoReq = "Eligible category spend"
            }
        }
    }

    val suggestedKeywords = remember(ruleType, category) {
        suggestedKeywordsFor(ruleType, category)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Benefit / Offer Rule" else "Add Benefit / Offer Rule") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Rule Type Selector (drives dynamic form below)
                Text(
                    text = "1. RULE TYPE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(RuleType.entries) { type ->
                        FilterChip(
                            selected = type == ruleType,
                            onClick = { onRuleTypeChanged(type) },
                            label = { Text(type.displayName) },
                            modifier = Modifier.testTag("rule_type_${type.name.lowercase()}")
                        )
                    }
                }

                // 2. Category Selector (tailors examples & suggested keywords)
                Text(
                    text = "2. CATEGORY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SpendCategory.entries) { cat ->
                        FilterChip(
                            selected = cat == category,
                            onClick = { category = cat },
                            label = { Text(cat.displayName) },
                            modifier = Modifier.testTag("rule_category_${cat.name.lowercase()}")
                        )
                    }
                }

                // 3. Benefit / Offer Title with dynamic placeholder
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(140) },
                    label = { Text("Offer / Benefit Title *") },
                    placeholder = { Text(titlePlaceholderFor(ruleType, category, currencySymbol)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_title_input")
                )

                // 4. Context-Aware Dynamic Details Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = iconForRuleType(ruleType),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${ruleType.displayName} • ${category.displayName} Details",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        when (ruleType) {
                            RuleType.MILESTONE -> {
                                PeriodChipRow(
                                    label = "USAGE LIMIT PERIOD",
                                    options = listOf(
                                        BenefitPeriod.MONTHLY,
                                        BenefitPeriod.QUARTERLY,
                                        BenefitPeriod.ANNUAL,
                                        BenefitPeriod.CARD_ANNIVERSARY
                                    ),
                                    selected = benefitPeriod,
                                    onSelect = { benefitPeriod = it }
                                )

                                OutlinedTextField(
                                    value = minSpendText,
                                    onValueChange = { minSpendText = sanitizeDecimalInput(it) },
                                    label = { Text("${benefitPeriod.displayName} Usage Limit to Reach ($currencySymbol) *") },
                                    placeholder = { Text("e.g. 100000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("milestone_spend_target_input")
                                )

                                Text(
                                    text = "MILESTONE REWARD TYPE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    MilestoneRewardType.entries.forEach { reward ->
                                        FilterChip(
                                            selected = reward == milestoneRewardType,
                                            onClick = { milestoneRewardType = reward },
                                            label = { Text(reward.displayName) },
                                            modifier = Modifier.testTag("milestone_reward_${reward.name.lowercase()}")
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = milestoneRewardDetail,
                                    onValueChange = { milestoneRewardDetail = it.take(180) },
                                    label = { Text(milestoneDetailLabelFor(milestoneRewardType)) },
                                    placeholder = { Text(milestoneDetailHintFor(milestoneRewardType, currencySymbol)) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("milestone_reward_detail_input")
                                )

                                OutlinedTextField(
                                    value = flatAmountText,
                                    onValueChange = { flatAmountText = sanitizeDecimalInput(it) },
                                    label = { Text("Estimated Reward Value ($currencySymbol)") },
                                    placeholder = { Text("e.g. 1000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            RuleType.FUEL -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = minSpendText,
                                        onValueChange = { minSpendText = sanitizeDecimalInput(it) },
                                        label = { Text("Min Fuel Txn ($currencySymbol)") },
                                        placeholder = { Text("400") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("fuel_min_txn_input")
                                    )
                                    OutlinedTextField(
                                        value = maxTxnAmountText,
                                        onValueChange = { maxTxnAmountText = sanitizeDecimalInput(it) },
                                        label = { Text("Max Fuel Txn ($currencySymbol)") },
                                        placeholder = { Text("4000") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("fuel_max_txn_input")
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = benefitPercentText,
                                        onValueChange = { benefitPercentText = sanitizeDecimalInput(it, maxLength = 6) },
                                        label = { Text("Waiver %") },
                                        placeholder = { Text("1.0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("fuel_waiver_percent_input")
                                    )
                                    OutlinedTextField(
                                        value = capAmountText,
                                        onValueChange = { capAmountText = sanitizeDecimalInput(it) },
                                        label = { Text("Max Waiver ($currencySymbol)") },
                                        placeholder = { Text("250") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("fuel_cap_amount_input")
                                    )
                                }

                                PeriodChipRow(
                                    label = "WAIVER CAP RESET PERIOD",
                                    options = listOf(
                                        BenefitPeriod.MONTHLY,
                                        BenefitPeriod.QUARTERLY,
                                        BenefitPeriod.ANNUAL,
                                        BenefitPeriod.PER_TXN
                                    ),
                                    selected = benefitPeriod,
                                    onSelect = { benefitPeriod = it }
                                )
                            }

                            RuleType.MERCHANT_OFFER, RuleType.DINING -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = benefitPercentText,
                                        onValueChange = { benefitPercentText = sanitizeDecimalInput(it, maxLength = 6) },
                                        label = { Text("Discount / CB %") },
                                        placeholder = { Text("10") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = flatAmountText,
                                        onValueChange = { flatAmountText = sanitizeDecimalInput(it) },
                                        label = { Text("Flat Off ($currencySymbol)") },
                                        placeholder = { Text("200") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = minSpendText,
                                        onValueChange = { minSpendText = sanitizeDecimalInput(it) },
                                        label = { Text("Min Txn Value ($currencySymbol)") },
                                        placeholder = { Text("500") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("offer_min_txn_input")
                                    )
                                    OutlinedTextField(
                                        value = capAmountText,
                                        onValueChange = { capAmountText = sanitizeDecimalInput(it) },
                                        label = { Text("Max Cap ($currencySymbol)") },
                                        placeholder = { Text("500") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                PeriodChipRow(
                                    label = "OFFER CAP / USAGE PERIOD",
                                    options = listOf(
                                        BenefitPeriod.PER_TXN,
                                        BenefitPeriod.MONTHLY,
                                        BenefitPeriod.QUARTERLY,
                                        BenefitPeriod.ANNUAL
                                    ),
                                    selected = benefitPeriod,
                                    onSelect = { benefitPeriod = it }
                                )

                                OutlinedTextField(
                                    value = couponCode,
                                    onValueChange = { couponCode = it.uppercase(Locale.ROOT).take(40) },
                                    label = { Text("Coupon / Promo Code (optional)") },
                                    placeholder = {
                                        Text(
                                            if (ruleType == RuleType.DINING) "e.g. DINE20, SWIGGYHDFC"
                                            else "e.g. SAVE500, AMZCARD10"
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.ConfirmationNumber,
                                            contentDescription = "Coupon code"
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("rule_coupon_code_input")
                                )
                            }

                            RuleType.LOUNGE -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = loungeVisitsText,
                                        onValueChange = { loungeVisitsText = it.filter { ch -> ch.isDigit() }.take(2) },
                                        label = { Text("Free Visits Count") },
                                        placeholder = { Text("2") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("lounge_visits_input")
                                    )
                                    OutlinedTextField(
                                        value = flatAmountText,
                                        onValueChange = { flatAmountText = sanitizeDecimalInput(it) },
                                        label = { Text("Value / Visit ($currencySymbol)") },
                                        placeholder = { Text("1200") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                PeriodChipRow(
                                    label = "LOUNGE QUOTA PERIOD",
                                    options = listOf(
                                        BenefitPeriod.QUARTERLY,
                                        BenefitPeriod.ANNUAL,
                                        BenefitPeriod.MONTHLY
                                    ),
                                    selected = benefitPeriod,
                                    onSelect = { benefitPeriod = it }
                                )

                                OutlinedTextField(
                                    value = minSpendText,
                                    onValueChange = { minSpendText = sanitizeDecimalInput(it) },
                                    label = { Text("Prior Spend Required to Unlock ($currencySymbol, 0 = none)") },
                                    placeholder = { Text("e.g. 50000 in previous quarter") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            RuleType.WELCOME -> {
                                OutlinedTextField(
                                    value = minSpendText,
                                    onValueChange = { minSpendText = sanitizeDecimalInput(it) },
                                    label = { Text("Joining Spend Target ($currencySymbol, 0 = on fee payment)") },
                                    placeholder = { Text("e.g. 10000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(
                                    text = "WELCOME REWARD TYPE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    MilestoneRewardType.entries.forEach { reward ->
                                        FilterChip(
                                            selected = reward == milestoneRewardType,
                                            onClick = { milestoneRewardType = reward },
                                            label = { Text(reward.displayName) }
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = milestoneRewardDetail,
                                    onValueChange = { milestoneRewardDetail = it.take(180) },
                                    label = { Text(milestoneDetailLabelFor(milestoneRewardType)) },
                                    placeholder = { Text(milestoneDetailHintFor(milestoneRewardType, currencySymbol)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = flatAmountText,
                                    onValueChange = { flatAmountText = sanitizeDecimalInput(it) },
                                    label = { Text("Estimated Welcome Value ($currencySymbol)") },
                                    placeholder = { Text("e.g. 500") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            RuleType.CATEGORY_REWARD, RuleType.TRAVEL -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = benefitPercentText,
                                        onValueChange = { benefitPercentText = sanitizeDecimalInput(it, maxLength = 6) },
                                        label = { Text("Reward Rate %") },
                                        placeholder = { Text("5.0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = pointsPer100Text,
                                        onValueChange = { pointsPer100Text = sanitizeDecimalInput(it, maxLength = 6) },
                                        label = { Text("Pts / 100 (opt)") },
                                        placeholder = { Text("5") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = minSpendText,
                                        onValueChange = { minSpendText = sanitizeDecimalInput(it) },
                                        label = { Text("Min Spend ($currencySymbol)") },
                                        placeholder = { Text("100") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = capAmountText,
                                        onValueChange = { capAmountText = sanitizeDecimalInput(it) },
                                        label = { Text("Max Cap ($currencySymbol)") },
                                        placeholder = { Text("0 = uncapped") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                PeriodChipRow(
                                    label = "CAP RESET PERIOD",
                                    options = listOf(
                                        BenefitPeriod.MONTHLY,
                                        BenefitPeriod.QUARTERLY,
                                        BenefitPeriod.ANNUAL,
                                        BenefitPeriod.PER_TXN
                                    ),
                                    selected = benefitPeriod,
                                    onSelect = { benefitPeriod = it }
                                )

                                OutlinedTextField(
                                    value = couponCode,
                                    onValueChange = { couponCode = it.uppercase(Locale.ROOT).take(40) },
                                    label = { Text("Promo / Portal Code (optional)") },
                                    placeholder = { Text("e.g. FLYHDFC, SAVE10") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.ConfirmationNumber,
                                            contentDescription = "Coupon code"
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // 5. Search Keywords (used by Advisor >3 letter keyword matching!)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = merchantKeywordsText,
                        onValueChange = { merchantKeywordsText = it },
                        label = {
                            Text(
                                when (ruleType) {
                                    RuleType.FUEL -> "Eligible Fuel Stations / Keywords *"
                                    RuleType.DINING -> "Restaurant / Dining App Keywords *"
                                    RuleType.LOUNGE -> "Lounge Network / Airport Keywords"
                                    RuleType.MILESTONE -> "Advisor Search Keywords (e.g. milestone, movie)"
                                    else -> "Merchant / Brand Keywords (comma separated) *"
                                }
                            )
                        },
                        placeholder = { Text(suggestedKeywords.joinToString(", ")) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rule_keywords_input")
                    )
                    Text(
                        text = "Tap to add keywords (Advisor ranks this card when 4+ matching letters are searched):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(suggestedKeywords) { chip ->
                            AssistChip(
                                onClick = { appendKeywordChip(chip) },
                                label = { Text("+ $chip") }
                            )
                        }
                    }
                }

                // 6. Payment Mode (for transactional rule types)
                if (ruleType != RuleType.MILESTONE && ruleType != RuleType.WELCOME) {
                    Text(
                        text = "PAYMENT MODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val options = listOf("ANY" to "Any") + PaymentMode.entries.map { it.name to it.shortLabel }
                        items(options) { (key, label) ->
                            FilterChip(
                                selected = selectedMode == key,
                                onClick = { selectedMode = key },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // 7. Validity, Channel, Exclusions & Notes
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = validityWindow,
                        onValueChange = { validityWindow = it.take(120) },
                        label = { Text(if (ruleType == RuleType.WELCOME) "Time Limit" else "Validity") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = promoReq,
                        onValueChange = { promoReq = it.take(160) },
                        label = { Text("Channel / How to Avail") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = excludedText,
                    onValueChange = { excludedText = it.take(300) },
                    label = { Text("Exclusions (optional)") },
                    placeholder = {
                        Text(
                            when (ruleType) {
                                RuleType.FUEL -> "GST on surcharge not waived; no base points"
                                RuleType.MILESTONE -> "Rent, Fuel, Wallet loads, Cash advances"
                                else -> "EMI, Wallet loads, Gift cards"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = conditionsText,
                    onValueChange = { conditionsText = it.take(500) },
                    label = { Text("Additional Conditions / Notes (auto-filled if blank)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val resolvedTitle = title.trim().ifBlank {
                        autoGenerateTitle(
                            ruleType = ruleType,
                            category = category,
                            currencySymbol = currencySymbol,
                            benefitPeriod = benefitPeriod,
                            milestoneRewardType = milestoneRewardType,
                            milestoneRewardDetail = milestoneRewardDetail,
                            benefitPercent = benefitPercentText.toDoubleOrNull() ?: 0.0,
                            loungeVisits = loungeVisitsText.toIntOrNull() ?: 0
                        )
                    }
                    if (resolvedTitle.isBlank()) {
                        errorText = "Please enter an offer or benefit title."
                        return@Button
                    }

                    val keywords = merchantKeywordsText
                        .split(",")
                        .map { it.trim().lowercase(Locale.ROOT) }
                        .filter { it.isNotBlank() }
                        .ifEmpty {
                            // Ensure specialized rules are still discoverable if user forgot keywords
                            when (ruleType) {
                                RuleType.MILESTONE -> listOf("milestone", milestoneRewardType.displayName.lowercase(Locale.ROOT))
                                RuleType.FUEL -> listOf("fuel", "hpcl", "bpcl", "shell")
                                RuleType.LOUNGE -> listOf("lounge", "airport")
                                RuleType.WELCOME -> listOf("welcome", "joining")
                                else -> emptyList()
                            }
                        }

                    val draftRule = CardBenefitRule(
                        ruleId = existingRule?.ruleId ?: "",
                        title = resolvedTitle,
                        ruleType = ruleType.name,
                        merchantKeywords = keywords,
                        category = category.name,
                        paymentModes = listOf(selectedMode),
                        benefitPercent = if (ruleType == RuleType.MILESTONE || ruleType == RuleType.LOUNGE || ruleType == RuleType.WELCOME) {
                            0.0
                        } else {
                            benefitPercentText.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
                        },
                        flatBenefitAmount = flatAmountText.toDoubleOrNull()?.coerceIn(0.0, 1_000_000.0) ?: 0.0,
                        pointsPer100 = pointsPer100Text.toDoubleOrNull()?.coerceIn(0.0, 500.0) ?: 0.0,
                        minSpend = minSpendText.toDoubleOrNull()?.coerceIn(0.0, 10_000_000.0) ?: 0.0,
                        maxTxnAmount = if (ruleType == RuleType.FUEL) {
                            maxTxnAmountText.toDoubleOrNull()?.coerceIn(0.0, 10_000_000.0) ?: 0.0
                        } else {
                            0.0
                        },
                        monthlyCapAmount = capAmountText.toDoubleOrNull()?.coerceIn(0.0, 10_000_000.0) ?: 0.0,
                        benefitPeriod = benefitPeriod.name,
                        milestoneRewardType = if (ruleType == RuleType.MILESTONE || ruleType == RuleType.WELCOME) {
                            milestoneRewardType.name
                        } else {
                            ""
                        },
                        milestoneRewardDetail = if (ruleType == RuleType.MILESTONE || ruleType == RuleType.WELCOME) {
                            milestoneRewardDetail.trim()
                        } else {
                            ""
                        },
                        couponCode = couponCode.trim().uppercase(Locale.ROOT),
                        loungeVisitsCount = if (ruleType == RuleType.LOUNGE) {
                            loungeVisitsText.toIntOrNull()?.coerceIn(0, 100) ?: 0
                        } else {
                            0
                        },
                        excludedCategories = excludedText.trim(),
                        conditionsText = conditionsText.trim(),
                        validityWindow = validityWindow.trim().ifBlank { "Ongoing" },
                        promoOrChannelReq = promoReq.trim().ifBlank { "Direct checkout" },
                        aiGenerated = false
                    )

                    val finalRule = if (draftRule.conditionsText.isBlank()) {
                        draftRule.copy(
                            conditionsText = BenefitRankingEngine.buildAutoConditionsSummary(
                                rule = draftRule,
                                currencySymbol = currencySymbol
                            )
                        )
                    } else {
                        draftRule
                    }

                    onSave(finalRule)
                },
                modifier = Modifier.testTag("save_rule_button")
            ) {
                Text("Save Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PeriodChipRow(
    label: String,
    options: List<BenefitPeriod>,
    selected: BenefitPeriod,
    onSelect: (BenefitPeriod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(options) { period ->
                FilterChip(
                    selected = period == selected,
                    onClick = { onSelect(period) },
                    label = { Text(period.displayName) },
                    modifier = Modifier.testTag("period_${period.name.lowercase()}")
                )
            }
        }
    }
}

private fun iconForRuleType(type: RuleType): ImageVector = when (type) {
    RuleType.MILESTONE -> Icons.Filled.EmojiEvents
    RuleType.FUEL -> Icons.Filled.LocalGasStation
    RuleType.DINING -> Icons.Filled.Restaurant
    RuleType.LOUNGE -> Icons.Filled.Weekend
    RuleType.TRAVEL -> Icons.Filled.Flight
    RuleType.WELCOME -> Icons.Filled.Star
    RuleType.MERCHANT_OFFER -> Icons.Filled.LocalOffer
    RuleType.CATEGORY_REWARD -> Icons.Filled.Star
}

private fun titlePlaceholderFor(
    ruleType: RuleType,
    category: SpendCategory,
    currency: String
): String = when (ruleType) {
    RuleType.MILESTONE -> "e.g. Quarterly Milestone: 2 Free Movie Tickets"
    RuleType.FUEL -> "e.g. 1% Fuel Surcharge Waiver (up to ${currency}250/mo)"
    RuleType.DINING -> "e.g. 20% Off Dining on Swiggy Dineout"
    RuleType.LOUNGE -> "e.g. 2 Complimentary Domestic Airport Lounges"
    RuleType.WELCOME -> "e.g. Welcome Bonus: 5,000 Reward Points"
    RuleType.TRAVEL -> "e.g. 10% Back on Flights & Hotels"
    RuleType.MERCHANT_OFFER -> when (category) {
        SpendCategory.ENTERTAINMENT -> "e.g. Buy 1 Get 1 Movie Tickets on BookMyShow"
        SpendCategory.GROCERIES -> "e.g. 10% Instant Discount on Blinkit"
        SpendCategory.UTILITIES -> "e.g. 5% Cashback on Electricity & Broadband"
        else -> "e.g. 5% Cashback on Amazon & Flipkart"
    }
    RuleType.CATEGORY_REWARD -> "e.g. 5% Accelerated Rewards on ${category.displayName}"
}

private fun milestoneDetailLabelFor(type: MilestoneRewardType): String = when (type) {
    MilestoneRewardType.REWARD_POINTS -> "Bonus Points Earned"
    MilestoneRewardType.MOVIE_TICKETS -> "Movie Tickets & Platform"
    MilestoneRewardType.GIFT_VOUCHER -> "Gift Voucher Brand & Amount"
    MilestoneRewardType.AIR_MILES -> "Bonus Air Miles"
    MilestoneRewardType.CASHBACK -> "Cashback / Statement Credit"
    MilestoneRewardType.LOUNGE_VISITS -> "Extra Lounge Visits"
    MilestoneRewardType.FEE_WAIVER -> "Annual Fee Waiver Detail"
    MilestoneRewardType.HOTEL_STAY -> "Hotel / Flight Voucher Detail"
    MilestoneRewardType.DINING_VOUCHER -> "Dining / Coffee Voucher Detail"
    MilestoneRewardType.OTHER -> "Milestone Reward Description"
}

private fun milestoneDetailHintFor(type: MilestoneRewardType, currency: String): String = when (type) {
    MilestoneRewardType.REWARD_POINTS -> "e.g. 10,000 Bonus Reward Points"
    MilestoneRewardType.MOVIE_TICKETS -> "e.g. 2 Free BookMyShow Tickets"
    MilestoneRewardType.GIFT_VOUCHER -> "e.g. ${currency}1,000 Amazon / Taj Voucher"
    MilestoneRewardType.AIR_MILES -> "e.g. 5,000 Air Miles"
    MilestoneRewardType.CASHBACK -> "e.g. ${currency}500 Statement Credit"
    MilestoneRewardType.LOUNGE_VISITS -> "e.g. 2 Domestic + 1 International Visit"
    MilestoneRewardType.FEE_WAIVER -> "e.g. 100% Renewal Fee Waived"
    MilestoneRewardType.HOTEL_STAY -> "e.g. 1 Free Domestic Flight / Taj Stay"
    MilestoneRewardType.DINING_VOUCHER -> "e.g. ${currency}500 Starbucks / Zomato Voucher"
    MilestoneRewardType.OTHER -> "e.g. Complimentary Golf Round / Subscription"
}

private fun suggestedKeywordsFor(ruleType: RuleType, category: SpendCategory): List<String> = when (ruleType) {
    RuleType.FUEL -> listOf("fuel", "hpcl", "bpcl", "indian oil", "shell", "petrol")
    RuleType.LOUNGE -> listOf("lounge", "airport", "dreamfolks", "priority pass")
    RuleType.MILESTONE -> listOf("milestone", "movie", "bookmyshow", "voucher", "points")
    RuleType.WELCOME -> listOf("welcome", "joining", "voucher", "activation")
    RuleType.DINING -> listOf("swiggy", "zomato", "dineout", "eazydiner", "starbucks", "dining")
    else -> when (category) {
        SpendCategory.SHOPPING -> listOf("amazon", "flipkart", "myntra", "apple", "nykaa", "croma")
        SpendCategory.GROCERIES -> listOf("bigbasket", "blinkit", "zepto", "instamart", "dmart")
        SpendCategory.DINING -> listOf("swiggy", "zomato", "dineout", "eazydiner", "starbucks")
        SpendCategory.TRAVEL -> listOf("makemytrip", "indigo", "smartbuy", "uber", "agoda", "flights")
        SpendCategory.UTILITIES -> listOf("electricity", "broadband", "airtel", "jio", "gas", "recharge")
        SpendCategory.FUEL -> listOf("fuel", "hpcl", "bpcl", "indian oil", "shell")
        SpendCategory.ENTERTAINMENT -> listOf("bookmyshow", "pvr", "inox", "netflix", "spotify")
        SpendCategory.ALL -> listOf("online", "shopping", "travel", "dining", "milestone")
    }
}

private fun autoGenerateTitle(
    ruleType: RuleType,
    category: SpendCategory,
    currencySymbol: String,
    benefitPeriod: BenefitPeriod,
    milestoneRewardType: MilestoneRewardType,
    milestoneRewardDetail: String,
    benefitPercent: Double,
    loungeVisits: Int
): String = when (ruleType) {
    RuleType.MILESTONE -> {
        val reward = milestoneRewardDetail.trim().ifBlank { milestoneRewardType.displayName }
        "${benefitPeriod.displayName} Milestone: $reward"
    }
    RuleType.FUEL -> {
        val pct = if (benefitPercent > 0.0) "${benefitPercent}%" else "1%"
        "$pct Fuel Surcharge Waiver"
    }
    RuleType.LOUNGE -> {
        val count = if (loungeVisits > 0) "$loungeVisits " else ""
        "${count}Complimentary Lounge Visits (${benefitPeriod.displayName})"
    }
    RuleType.WELCOME -> {
        val reward = milestoneRewardDetail.trim().ifBlank { milestoneRewardType.displayName }
        "Welcome Perk: $reward"
    }
    else -> {
        val pct = if (benefitPercent > 0.0) "${benefitPercent}% " else ""
        "${pct}${ruleType.displayName} (${category.displayName})"
    }
}
