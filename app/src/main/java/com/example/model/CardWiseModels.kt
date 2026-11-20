package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CardNetwork(val displayName: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    RUPAY("RuPay"),
    AMEX("Amex"),
    DISCOVER("Discover"),
    DINERS("Diners Club");

    companion object {
        fun fromRaw(raw: String): CardNetwork =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: VISA
    }
}

enum class CardTheme(val displayName: String) {
    OBSIDIAN("Obsidian"),
    EMERALD("Emerald"),
    SAPPHIRE("Sapphire"),
    GOLD("Gold"),
    CRIMSON("Crimson"),
    SLATE("Slate");

    companion object {
        fun fromRaw(raw: String): CardTheme =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: OBSIDIAN
    }
}

enum class RewardType(val displayName: String) {
    CASHBACK("Cashback"),
    POINTS("Reward Points"),
    MILES("Air Miles");

    companion object {
        fun fromRaw(raw: String): RewardType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: CASHBACK
    }
}

enum class SpendCategory(val displayName: String) {
    SHOPPING("Shopping"),
    GROCERIES("Groceries"),
    DINING("Dining"),
    TRAVEL("Travel"),
    UTILITIES("Utilities"),
    FUEL("Fuel"),
    ENTERTAINMENT("Entertainment"),
    ALL("All Spends");

    companion object {
        fun fromRaw(raw: String): SpendCategory =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: SHOPPING
    }
}

enum class PaymentMode(val displayName: String, val shortLabel: String) {
    ONLINE("Online Checkout", "Online"),
    PORTAL("Co-branded / Bank Portal", "Portal"),
    EMI("No-Cost / Card EMI", "EMI"),
    IN_STORE("In-Store / Tap & Pay", "In-Store");

    companion object {
        fun fromRaw(raw: String): PaymentMode =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: ONLINE
    }
}

enum class RuleType(val displayName: String) {
    MERCHANT_OFFER("Merchant Offer"),
    CATEGORY_REWARD("Category Reward"),
    MILESTONE("Milestone Bonus"),
    LOUNGE("Lounge Access"),
    FUEL("Fuel Waiver"),
    WELCOME("Welcome Perk"),
    DINING("Dining Privilege"),
    TRAVEL("Travel Benefit");

    companion object {
        fun fromRaw(raw: String): RuleType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: CATEGORY_REWARD
    }
}

enum class BenefitPeriod(val displayName: String, val shortLabel: String) {
    PER_TXN("Per Transaction", "per txn"),
    MONTHLY("Monthly", "per month"),
    QUARTERLY("Quarterly", "per quarter"),
    ANNUAL("Annual", "per year"),
    CARD_ANNIVERSARY("Card Anniversary", "per year"),
    ONE_TIME("One-Time", "one-time");

    companion object {
        fun fromRaw(raw: String): BenefitPeriod =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: MONTHLY
    }
}

enum class MilestoneRewardType(val displayName: String) {
    REWARD_POINTS("Reward Points"),
    MOVIE_TICKETS("Movie Tickets"),
    GIFT_VOUCHER("Gift Voucher"),
    AIR_MILES("Air Miles"),
    CASHBACK("Cashback / Statement Credit"),
    LOUNGE_VISITS("Lounge Visits"),
    FEE_WAIVER("Annual Fee Waiver"),
    HOTEL_STAY("Hotel / Flight Voucher"),
    DINING_VOUCHER("Dining / Coffee Voucher"),
    OTHER("Other Reward");

    companion object {
        fun fromRaw(raw: String): MilestoneRewardType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: REWARD_POINTS
    }
}

@Keep
@IgnoreExtraProperties
data class CardBenefitRule(
    val ruleId: String = "",
    val title: String = "",
    val ruleType: String = RuleType.CATEGORY_REWARD.name,
    val merchantKeywords: List<String> = emptyList(),
    val category: String = SpendCategory.ALL.name,
    val paymentModes: List<String> = listOf("ANY"),
    val benefitPercent: Double = 0.0,
    val flatBenefitAmount: Double = 0.0,
    val pointsPer100: Double = 0.0,
    val minSpend: Double = 0.0,
    val maxTxnAmount: Double = 0.0,
    val monthlyCapAmount: Double = 0.0,
    val benefitPeriod: String = BenefitPeriod.MONTHLY.name,
    val milestoneRewardType: String = "",
    val milestoneRewardDetail: String = "",
    val couponCode: String = "",
    val loungeVisitsCount: Int = 0,
    val excludedCategories: String = "",
    val conditionsText: String = "",
    val validityWindow: String = "Ongoing",
    val promoOrChannelReq: String = "Auto-applied",
    val aiGenerated: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "ruleId" to ruleId.ifBlank { "rule_${System.currentTimeMillis()}" }.take(64),
        "title" to title.trim().take(140),
        "ruleType" to RuleType.fromRaw(ruleType).name,
        "merchantKeywords" to merchantKeywords
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .distinct()
            .take(15),
        "category" to SpendCategory.fromRaw(category).name,
        "paymentModes" to paymentModes
            .map { it.trim().uppercase(Locale.ROOT) }
            .filter { it in setOf("ONLINE", "PORTAL", "EMI", "IN_STORE", "ANY") }
            .distinct()
            .ifEmpty { listOf("ANY") },
        "benefitPercent" to benefitPercent.coerceIn(0.0, 100.0),
        "flatBenefitAmount" to flatBenefitAmount.coerceIn(0.0, 1_000_000.0),
        "pointsPer100" to pointsPer100.coerceIn(0.0, 500.0),
        "minSpend" to minSpend.coerceIn(0.0, 10_000_000.0),
        "maxTxnAmount" to maxTxnAmount.coerceIn(0.0, 10_000_000.0),
        "monthlyCapAmount" to monthlyCapAmount.coerceIn(0.0, 10_000_000.0),
        "benefitPeriod" to BenefitPeriod.fromRaw(benefitPeriod).name,
        "milestoneRewardType" to milestoneRewardType.trim().take(60),
        "milestoneRewardDetail" to milestoneRewardDetail.trim().take(180),
        "couponCode" to couponCode.trim().uppercase(Locale.ROOT).take(40),
        "loungeVisitsCount" to loungeVisitsCount.coerceIn(0, 100),
        "excludedCategories" to excludedCategories.trim().take(300),
        "conditionsText" to conditionsText.trim().take(500),
        "validityWindow" to validityWindow.trim().ifBlank { "Ongoing" }.take(120),
        "promoOrChannelReq" to promoOrChannelReq.trim().ifBlank { "Auto-applied" }.take(160),
        "aiGenerated" to aiGenerated
    )
}

@Keep
@IgnoreExtraProperties
data class CreditCard(
    @DocumentId val id: String = "",
    val userId: String = "",
    val issuer: String = "",
    val cardName: String = "",
    val network: String = CardNetwork.VISA.name,
    val lastFour: String = "0000",
    val creditLimit: Double = 0.0,
    val statementBalance: Double = 0.0,
    val billingDay: Long = 15L,
    val dueDay: Long = 5L,
    val currencySymbol: String = "₹",
    val cardTheme: String = CardTheme.OBSIDIAN.name,
    val baseRewardRatePercent: Double = 1.0,
    val rewardType: String = RewardType.CASHBACK.name,
    val pointValueInCurrency: Double = 1.0,
    val aiFetched: Boolean = false,
    val aiSummary: String = "",
    val aiLastUpdatedEpochMs: Long = 0L,
    val sampleData: Boolean = false,
    val rules: List<CardBenefitRule> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val displayTitle: String
        get() = "$issuer $cardName".trim()

    val maskedEnding: String
        get() = "•••• $lastFour"

    val utilizationRatio: Float
        get() = if (creditLimit <= 0.0) 0f else (statementBalance / creditLimit).toFloat().coerceIn(0f, 1f)

    fun toCreateMap(uid: String): Map<String, Any> = mapOf(
        "userId" to uid,
        "issuer" to issuer.trim().take(80),
        "cardName" to cardName.trim().take(100),
        "network" to CardNetwork.fromRaw(network).name,
        "lastFour" to lastFour.filter { it.isDigit() }.takeLast(4).padStart(4, '0'),
        "creditLimit" to creditLimit.coerceIn(0.0, 100_000_000.0),
        "statementBalance" to statementBalance.coerceIn(0.0, 100_000_000.0),
        "billingDay" to billingDay.coerceIn(1L, 31L),
        "dueDay" to dueDay.coerceIn(1L, 31L),
        "currencySymbol" to currencySymbol.trim().ifBlank { "₹" }.take(5),
        "cardTheme" to CardTheme.fromRaw(cardTheme).name,
        "baseRewardRatePercent" to baseRewardRatePercent.coerceIn(0.0, 50.0),
        "rewardType" to RewardType.fromRaw(rewardType).name,
        "pointValueInCurrency" to pointValueInCurrency.coerceIn(0.0, 100.0),
        "aiFetched" to aiFetched,
        "aiSummary" to aiSummary.trim().take(1200),
        "aiLastUpdatedEpochMs" to aiLastUpdatedEpochMs.coerceAtLeast(0L),
        "sampleData" to sampleData,
        "rules" to rules.take(30).map { it.toMap() },
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toFullUpdateMap(): Map<String, Any> = mapOf(
        "issuer" to issuer.trim().take(80),
        "cardName" to cardName.trim().take(100),
        "network" to CardNetwork.fromRaw(network).name,
        "lastFour" to lastFour.filter { it.isDigit() }.takeLast(4).padStart(4, '0'),
        "creditLimit" to creditLimit.coerceIn(0.0, 100_000_000.0),
        "statementBalance" to statementBalance.coerceIn(0.0, 100_000_000.0),
        "billingDay" to billingDay.coerceIn(1L, 31L),
        "dueDay" to dueDay.coerceIn(1L, 31L),
        "currencySymbol" to currencySymbol.trim().ifBlank { "₹" }.take(5),
        "cardTheme" to CardTheme.fromRaw(cardTheme).name,
        "baseRewardRatePercent" to baseRewardRatePercent.coerceIn(0.0, 50.0),
        "rewardType" to RewardType.fromRaw(rewardType).name,
        "pointValueInCurrency" to pointValueInCurrency.coerceIn(0.0, 100.0),
        "aiFetched" to aiFetched,
        "aiSummary" to aiSummary.trim().take(1200),
        "aiLastUpdatedEpochMs" to aiLastUpdatedEpochMs.coerceAtLeast(0L),
        "rules" to rules.take(30).map { it.toMap() },
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class PurchaseLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val cardId: String = "",
    val cardDisplayName: String = "",
    val cardNetwork: String = CardNetwork.VISA.name,
    val merchantName: String = "",
    val itemNote: String = "",
    val category: String = SpendCategory.SHOPPING.name,
    val paymentMode: String = PaymentMode.ONLINE.name,
    val amount: Double = 0.0,
    val currencySymbol: String = "₹",
    val matchedRuleId: String = "",
    val matchedRuleTitle: String = "",
    val expectedBenefitAmount: Double = 0.0,
    val effectiveReturnPercent: Double = 0.0,
    val billingCycleKey: String = currentBillingCycleKey(),
    val sampleData: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = mapOf(
        "userId" to uid,
        "cardId" to cardId,
        "cardDisplayName" to cardDisplayName.trim().take(140),
        "cardNetwork" to cardNetwork.trim().ifBlank { CardNetwork.VISA.name }.take(30),
        "merchantName" to merchantName.trim().ifBlank { "General Purchase" }.take(120),
        "itemNote" to itemNote.trim().take(300),
        "category" to SpendCategory.fromRaw(category).name,
        "paymentMode" to PaymentMode.fromRaw(paymentMode).name,
        "amount" to amount.coerceIn(0.01, 100_000_000.0),
        "currencySymbol" to currencySymbol.trim().ifBlank { "₹" }.take(5),
        "matchedRuleId" to matchedRuleId.trim().take(128),
        "matchedRuleTitle" to matchedRuleTitle.trim().take(160),
        "expectedBenefitAmount" to expectedBenefitAmount.coerceIn(0.0, 10_000_000.0),
        "effectiveReturnPercent" to effectiveReturnPercent.coerceIn(0.0, 100.0),
        "billingCycleKey" to billingCycleKey.trim().ifBlank { currentBillingCycleKey() }.take(20),
        "sampleData" to sampleData,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class UserMeta(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sampleSeeded: Boolean = false,
    val preferredCurrency: String = "₹",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

fun currentBillingCycleKey(nowMs: Long = System.currentTimeMillis()): String {
    val formatter = SimpleDateFormat("yyyy-MM", Locale.US)
    return formatter.format(Date(nowMs))
}

fun formatCurrency(amount: Double, symbol: String = "₹"): String {
    return if (amount % 1.0 == 0.0) {
        String.format(Locale.US, "%s%,.0f", symbol, amount)
    } else {
        String.format(Locale.US, "%s%,.2f", symbol, amount)
    }
}

fun formatPercent(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f%%", value)
    } else {
        String.format(Locale.US, "%.2f%%", value)
    }
}

fun formatTimestampReadable(epochMs: Long): String {
    if (epochMs <= 0L) return "Not synced yet"
    val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
    return formatter.format(Date(epochMs))
}

/**
 * Sanitizes decimal number input so at most one decimal point is permitted,
 * preventing malformed values like "100.5.2" from causing parse failures.
 */
fun sanitizeDecimalInput(raw: String, maxLength: Int = 14): String {
    val sb = StringBuilder()
    var hasDot = false
    for (ch in raw) {
        when {
            ch.isDigit() -> sb.append(ch)
            ch == '.' && !hasDot -> {
                if (sb.isEmpty()) sb.append('0')
                sb.append('.')
                hasDot = true
            }
            ch == '.' && hasDot -> break
        }
        if (sb.length >= maxLength) break
    }
    return sb.toString()
}

fun isSafeWebUri(uri: String): Boolean {
    val trimmed = uri.trim().lowercase(Locale.ROOT)
    return trimmed.startsWith("https://") || trimmed.startsWith("http://")
}
