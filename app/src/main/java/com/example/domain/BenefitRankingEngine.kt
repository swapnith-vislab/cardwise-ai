package com.example.domain

import com.example.model.BenefitPeriod
import com.example.model.CardBenefitRule
import com.example.model.CreditCard
import com.example.model.MilestoneRewardType
import com.example.model.PaymentMode
import com.example.model.PurchaseLog
import com.example.model.RewardType
import com.example.model.RuleType
import com.example.model.SpendCategory
import com.example.model.currentBillingCycleKey
import com.example.model.formatCurrency
import java.util.Locale

data class PurchaseSearchQuery(
    val merchantText: String = "",
    val category: SpendCategory = SpendCategory.SHOPPING,
    val amount: Double? = null,
    val paymentMode: PaymentMode = PaymentMode.ONLINE,
    val billingCycleKey: String = currentBillingCycleKey()
)

enum class MatchTier(val label: String) {
    MERCHANT_OFFER("Keyword Match"),
    CATEGORY_ACCELERATOR("Category Match"),
    BASE_RATE("Base Card Rate")
}

data class RankedCardRecommendation(
    val card: CreditCard,
    val rank: Int,
    val matchTier: MatchTier,
    val matchedRule: CardBenefitRule?,
    val headlineOffer: String,
    val estimatedBenefitAmount: Double,
    val estimatedPointsEarned: Double,
    val effectiveReturnPercent: Double,
    val evaluatedAmount: Double,
    val amountWasProvided: Boolean,
    val monthlyCapAmount: Double,
    val usedCapInCycle: Double,
    val remainingCapInCycle: Double?,
    val capReached: Boolean,
    val minSpendNotMet: Boolean,
    val conditionsSummary: String,
    val excludedCategories: String,
    val promoOrChannelReq: String,
    val validityWindow: String,
    val couponCode: String = "",
    val matchedKeywords: List<String> = emptyList(),
    val warnings: List<String>
)

data class CapUsageSummary(
    val cardId: String,
    val cardTitle: String,
    val cardLastFour: String,
    val currencySymbol: String,
    val ruleId: String,
    val ruleTitle: String,
    val usedAmount: Double,
    val monthlyCap: Double
) {
    val remainingAmount: Double
        get() = (monthlyCap - usedAmount).coerceAtLeast(0.0)

    val progressFraction: Float
        get() = if (monthlyCap <= 0.0) 0f else (usedAmount / monthlyCap).toFloat().coerceIn(0f, 1f)
}

object BenefitRankingEngine {

    /**
     * Strictly requires MORE than 3 characters (i.e. at least 4 characters) in the search bar
     * before ranked cards are returned.
     */
    const val MIN_SEARCH_QUERY_LENGTH = 4

    private const val DEFAULT_REFERENCE_AMOUNT = 10_000.0

    fun computeCapUsageByRule(
        transactions: List<PurchaseLog>,
        billingCycleKey: String = currentBillingCycleKey()
    ): Map<Pair<String, String>, Double> {
        val usage = mutableMapOf<Pair<String, String>, Double>()
        for (tx in transactions) {
            if (tx.billingCycleKey == billingCycleKey && tx.matchedRuleId.isNotBlank()) {
                val key = tx.cardId to tx.matchedRuleId
                usage[key] = (usage[key] ?: 0.0) + tx.expectedBenefitAmount
            }
        }
        return usage
    }

    fun buildActiveCapSummaries(
        cards: List<CreditCard>,
        transactions: List<PurchaseLog>,
        billingCycleKey: String = currentBillingCycleKey()
    ): List<CapUsageSummary> {
        val usageMap = computeCapUsageByRule(transactions, billingCycleKey)
        val summaries = mutableListOf<CapUsageSummary>()
        for (card in cards) {
            for (rule in card.rules) {
                if (rule.monthlyCapAmount > 0.0 && rule.ruleType != RuleType.MILESTONE.name && rule.ruleType != RuleType.LOUNGE.name) {
                    val used = usageMap[card.id to rule.ruleId] ?: 0.0
                    summaries.add(
                        CapUsageSummary(
                            cardId = card.id,
                            cardTitle = card.displayTitle,
                            cardLastFour = card.lastFour,
                            currencySymbol = card.currencySymbol,
                            ruleId = rule.ruleId,
                            ruleTitle = rule.title,
                            usedAmount = used,
                            monthlyCap = rule.monthlyCapAmount
                        )
                    )
                }
            }
        }
        return summaries.sortedByDescending { it.progressFraction }
    }

    /**
     * Ranks ONLY cards that have at least one benefit rule whose keywords (or coupon code)
     * match the entered search text, and ONLY when more than 3 characters (4+) are entered.
     */
    fun rankCards(
        cards: List<CreditCard>,
        transactions: List<PurchaseLog>,
        query: PurchaseSearchQuery
    ): List<RankedCardRecommendation> {
        if (cards.isEmpty()) return emptyList()

        val normalizedMerchant = query.merchantText.trim().lowercase(Locale.ROOT)
        if (normalizedMerchant.length < MIN_SEARCH_QUERY_LENGTH) {
            return emptyList()
        }

        val usageMap = computeCapUsageByRule(transactions, query.billingCycleKey)
        val evalAmount = query.amount?.takeIf { it > 0.0 } ?: DEFAULT_REFERENCE_AMOUNT
        val amountWasProvided = query.amount != null && query.amount > 0.0

        val rawScored = cards.mapNotNull { card ->
            evaluateCardForKeywordMatch(
                card = card,
                normalizedMerchant = normalizedMerchant,
                query = query,
                evalAmount = evalAmount,
                amountWasProvided = amountWasProvided,
                usageMap = usageMap
            )
        }

        val sorted = rawScored.sortedWith(
            compareByDescending<RankedCardRecommendation> { it.estimatedBenefitAmount }
                .thenByDescending { it.effectiveReturnPercent }
                .thenBy { it.warnings.size }
                .thenBy { it.card.displayTitle }
        )

        return sorted.mapIndexed { idx, item -> item.copy(rank = idx + 1) }
    }

    private fun evaluateCardForKeywordMatch(
        card: CreditCard,
        normalizedMerchant: String,
        query: PurchaseSearchQuery,
        evalAmount: Double,
        amountWasProvided: Boolean,
        usageMap: Map<Pair<String, String>, Double>
    ): RankedCardRecommendation? {
        val matchedCandidates = mutableListOf<RankedCardRecommendation>()

        for (rule in card.rules) {
            val matchedKeywords = findMatchingKeywords(normalizedMerchant, rule)
            if (matchedKeywords.isEmpty()) continue

            val ruleTypeEnum = RuleType.fromRaw(rule.ruleType)
            val period = BenefitPeriod.fromRaw(rule.benefitPeriod)
            val exclusionsLower = rule.excludedCategories.lowercase(Locale.ROOT)
            val isExcludedByMode = query.paymentMode == PaymentMode.EMI && exclusionsLower.contains("emi")
            val modeMatches = rule.paymentModes.any {
                it.equals("ANY", ignoreCase = true) || it.equals(query.paymentMode.name, ignoreCase = true)
            }

            val usedCap = usageMap[card.id to rule.ruleId] ?: 0.0
            val hasCap = rule.monthlyCapAmount > 0.0 && period != BenefitPeriod.PER_TXN
            val remainingCap = if (hasCap) (rule.monthlyCapAmount - usedCap).coerceAtLeast(0.0) else null
            val capReached = hasCap && (remainingCap ?: 0.0) <= 0.0

            val minSpendNotMet = amountWasProvided && rule.minSpend > 0.0 && evalAmount < rule.minSpend
            val maxTxnExceeded = amountWasProvided && rule.maxTxnAmount > 0.0 && evalAmount > rule.maxTxnAmount
            val warnings = mutableListOf<String>()

            if (isExcludedByMode) {
                warnings.add("EMI transactions are excluded under this offer")
            }
            if (!modeMatches) {
                warnings.add("Requires ${rule.promoOrChannelReq.ifBlank { rule.paymentModes.joinToString("/") }}")
            }
            if (minSpendNotMet) {
                val label = if (ruleTypeEnum == RuleType.MILESTONE) "Milestone target" else "Min spend"
                warnings.add("$label ${formatCurrency(rule.minSpend, card.currencySymbol)} not met")
            }
            if (maxTxnExceeded) {
                warnings.add("Max eligible txn ${formatCurrency(rule.maxTxnAmount, card.currencySymbol)} exceeded")
            }
            if (capReached) {
                warnings.add("${period.displayName} benefit cap exhausted for ${query.billingCycleKey}")
            }

            val eligibleForFullBenefit = !isExcludedByMode && modeMatches && !minSpendNotMet && !maxTxnExceeded && !capReached

            val rawBenefit = if (!eligibleForFullBenefit) {
                0.0
            } else {
                val pctValue = evalAmount * (rule.benefitPercent / 100.0)
                val flatValue = rule.flatBenefitAmount
                val combined = maxOf(pctValue, flatValue)
                val perTxnCapped = if (period == BenefitPeriod.PER_TXN && rule.monthlyCapAmount > 0.0) {
                    minOf(combined, rule.monthlyCapAmount)
                } else {
                    combined
                }
                if (remainingCap != null) minOf(perTxnCapped, remainingCap) else perTxnCapped
            }

            val effectivePercent = if (evalAmount > 0.0) (rawBenefit / evalAmount) * 100.0 else 0.0
            val pointsEarned = if (card.rewardType != RewardType.CASHBACK.name && card.pointValueInCurrency > 0.0) {
                if (rule.pointsPer100 > 0.0 && eligibleForFullBenefit) {
                    val uncappedPts = (evalAmount / 100.0) * rule.pointsPer100
                    if (remainingCap != null) minOf(uncappedPts, remainingCap / card.pointValueInCurrency) else uncappedPts
                } else {
                    rawBenefit / card.pointValueInCurrency
                }
            } else {
                0.0
            }

            val resolvedSummary = rule.conditionsText.ifBlank {
                buildAutoConditionsSummary(rule, card.currencySymbol)
            }

            matchedCandidates.add(
                RankedCardRecommendation(
                    card = card,
                    rank = 0,
                    matchTier = MatchTier.MERCHANT_OFFER,
                    matchedRule = rule,
                    headlineOffer = rule.title,
                    estimatedBenefitAmount = rawBenefit,
                    estimatedPointsEarned = pointsEarned,
                    effectiveReturnPercent = effectivePercent,
                    evaluatedAmount = evalAmount,
                    amountWasProvided = amountWasProvided,
                    monthlyCapAmount = rule.monthlyCapAmount,
                    usedCapInCycle = usedCap,
                    remainingCapInCycle = remainingCap,
                    capReached = capReached,
                    minSpendNotMet = minSpendNotMet,
                    conditionsSummary = resolvedSummary,
                    excludedCategories = rule.excludedCategories,
                    promoOrChannelReq = rule.promoOrChannelReq,
                    validityWindow = rule.validityWindow,
                    couponCode = rule.couponCode,
                    matchedKeywords = matchedKeywords,
                    warnings = warnings
                )
            )
        }

        if (matchedCandidates.isEmpty()) return null

        return matchedCandidates.maxWith(
            compareBy<RankedCardRecommendation> { it.estimatedBenefitAmount }
                .thenBy { candidate ->
                    val exact = candidate.matchedKeywords.any { it.equals(normalizedMerchant, ignoreCase = true) }
                    if (exact) 1 else 0
                }
                .thenBy { candidate ->
                    val ruleCat = candidate.matchedRule?.category
                    if (query.category != SpendCategory.ALL && ruleCat.equals(query.category.name, ignoreCase = true)) 1 else 0
                }
                .thenBy { -it.warnings.size }
                .thenBy { it.matchedRule?.benefitPercent ?: 0.0 }
        )
    }

    internal fun findMatchingKeywords(normalizedQuery: String, rule: CardBenefitRule): List<String> {
        if (normalizedQuery.length < MIN_SEARCH_QUERY_LENGTH) return emptyList()

        val compactQuery = normalizedQuery.replace(Regex("[\\s,./-]+"), "")
        val queryTokens = normalizedQuery
            .split(Regex("[\\s,./-]+"))
            .map { it.trim() }
            .filter { it.length >= MIN_SEARCH_QUERY_LENGTH }

        val matches = mutableListOf<String>()

        for (keyword in rule.merchantKeywords) {
            val cleanKeyword = keyword.trim().lowercase(Locale.ROOT)
            if (cleanKeyword.length < 2) continue
            val compactKeyword = cleanKeyword.replace(Regex("[\\s,./-]+"), "")

            val directMatch = normalizedQuery.contains(cleanKeyword) ||
                cleanKeyword.contains(normalizedQuery) ||
                (compactQuery.length >= MIN_SEARCH_QUERY_LENGTH && compactKeyword.length >= 2 &&
                    (compactQuery.contains(compactKeyword) || compactKeyword.contains(compactQuery)))
            val tokenMatch = !directMatch && queryTokens.any { token ->
                token.contains(cleanKeyword) || cleanKeyword.contains(token) ||
                    token.contains(compactKeyword) || compactKeyword.contains(token)
            }
            if (directMatch || tokenMatch) {
                matches.add(cleanKeyword)
            }
        }

        val cleanCoupon = rule.couponCode.trim().lowercase(Locale.ROOT)
        if (cleanCoupon.length >= MIN_SEARCH_QUERY_LENGTH &&
            (cleanCoupon.contains(normalizedQuery) || normalizedQuery.contains(cleanCoupon) ||
                cleanCoupon.contains(compactQuery) || compactQuery.contains(cleanCoupon))
        ) {
            matches.add("code:${rule.couponCode.trim().uppercase(Locale.ROOT)}")
        }

        return matches.distinct()
    }

    fun buildAutoConditionsSummary(rule: CardBenefitRule, currencySymbol: String): String {
        val period = BenefitPeriod.fromRaw(rule.benefitPeriod)
        return when (RuleType.fromRaw(rule.ruleType)) {
            RuleType.MILESTONE -> buildString {
                if (rule.minSpend > 0.0) {
                    append("Reach ${formatCurrency(rule.minSpend, currencySymbol)} spend (${period.displayName.lowercase(Locale.ROOT)})")
                } else {
                    append("${period.displayName} milestone benefit")
                }
                val rewardName = rule.milestoneRewardType.takeIf { it.isNotBlank() }
                    ?.let { MilestoneRewardType.fromRaw(it).displayName }
                    ?: "Milestone Reward"
                append(" to unlock $rewardName")
                if (rule.milestoneRewardDetail.isNotBlank()) {
                    append(": ${rule.milestoneRewardDetail}")
                }
                append(".")
            }

            RuleType.FUEL -> buildString {
                val pct = if (rule.benefitPercent > 0.0) "${rule.benefitPercent}%" else "Fuel"
                append("$pct surcharge waiver")
                if (rule.minSpend > 0.0 && rule.maxTxnAmount > 0.0) {
                    append(" on fuel spends from ${formatCurrency(rule.minSpend, currencySymbol)} to ${formatCurrency(rule.maxTxnAmount, currencySymbol)}")
                } else if (rule.minSpend > 0.0) {
                    append(" on min fuel purchase of ${formatCurrency(rule.minSpend, currencySymbol)}")
                }
                if (rule.monthlyCapAmount > 0.0) {
                    append(", up to ${formatCurrency(rule.monthlyCapAmount, currencySymbol)} ${period.shortLabel}")
                }
                append(".")
            }

            RuleType.LOUNGE -> buildString {
                val visits = if (rule.loungeVisitsCount > 0) "${rule.loungeVisitsCount} complimentary visits ${period.shortLabel}" else "Complimentary lounge access"
                append(visits)
                if (rule.minSpend > 0.0) {
                    append(" • Requires ${formatCurrency(rule.minSpend, currencySymbol)} prior spend")
                }
                append(".")
            }

            else -> buildString {
                val parts = mutableListOf<String>()
                if (rule.minSpend > 0.0) {
                    parts.add("Min txn ${formatCurrency(rule.minSpend, currencySymbol)}")
                }
                if (rule.monthlyCapAmount > 0.0) {
                    parts.add("Cap ${formatCurrency(rule.monthlyCapAmount, currencySymbol)} ${period.shortLabel}")
                }
                if (rule.couponCode.isNotBlank()) {
                    parts.add("Coupon: ${rule.couponCode.uppercase(Locale.ROOT)}")
                }
                if (parts.isEmpty()) {
                    append("Standard card offer terms apply.")
                } else {
                    append(parts.joinToString(" • "))
                }
            }
        }
    }
}
