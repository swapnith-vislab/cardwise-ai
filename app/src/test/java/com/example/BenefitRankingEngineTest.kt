package com.example

import com.example.domain.BenefitRankingEngine
import com.example.domain.MatchTier
import com.example.domain.PurchaseSearchQuery
import com.example.model.BenefitPeriod
import com.example.model.CardBenefitRule
import com.example.model.CreditCard
import com.example.model.MilestoneRewardType
import com.example.model.PaymentMode
import com.example.model.PurchaseLog
import com.example.model.RuleType
import com.example.model.SampleDataFactory
import com.example.model.SpendCategory
import com.example.model.currentBillingCycleKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BenefitRankingEngineTest {

    @Test
    fun `queries with 3 or fewer characters return empty ranked list`() {
        val cards = SampleDataFactory.createSampleCards("test_uid").map { it.second }
        val shortQueries = listOf("", " ", "a", "am", "ama", "   ama   ", "zmt")

        for (q in shortQueries) {
            val ranked = BenefitRankingEngine.rankCards(
                cards = cards,
                transactions = emptyList(),
                query = PurchaseSearchQuery(
                    merchantText = q,
                    category = SpendCategory.SHOPPING,
                    amount = 10_000.0,
                    paymentMode = PaymentMode.ONLINE
                )
            )
            assertTrue("Query '$q' (<= 3 chars) must return empty list", ranked.isEmpty())
        }
    }

    @Test
    fun `four letter keyword query returns only cards matching rule keywords`() {
        val cards = SampleDataFactory.createSampleCards("test_uid").map { it.second }

        // "amaz" (4 letters) matches the 3 cards with "amazon" keyword, excluding Axis ACE
        val amazonRanked = BenefitRankingEngine.rankCards(
            cards = cards,
            transactions = emptyList(),
            query = PurchaseSearchQuery(
                merchantText = "amaz",
                category = SpendCategory.ALL,
                amount = 10_000.0,
                paymentMode = PaymentMode.ONLINE
            )
        )
        assertEquals(3, amazonRanked.size)
        assertTrue(amazonRanked.none { it.card.id == SampleDataFactory.SAMPLE_CARD_AXIS_ID })
        assertTrue(amazonRanked.all { it.matchTier == MatchTier.MERCHANT_OFFER })

        // "shel" (4 letters) matches only ICICI Amazon Pay's fuel rule ("shell")
        val fuelRanked = BenefitRankingEngine.rankCards(
            cards = cards,
            transactions = emptyList(),
            query = PurchaseSearchQuery(
                merchantText = "shel",
                category = SpendCategory.FUEL,
                amount = 2_000.0,
                paymentMode = PaymentMode.IN_STORE
            )
        )
        assertEquals(1, fuelRanked.size)
        assertEquals(SampleDataFactory.SAMPLE_CARD_AMAZON_ID, fuelRanked.first().card.id)

        // Unmatched 4+ letter query returns empty list (no base-rate fallback cards)
        val noMatch = BenefitRankingEngine.rankCards(
            cards = cards,
            transactions = emptyList(),
            query = PurchaseSearchQuery(
                merchantText = "zzzzunknown",
                category = SpendCategory.SHOPPING,
                amount = 10_000.0,
                paymentMode = PaymentMode.ONLINE
            )
        )
        assertTrue(noMatch.isEmpty())
    }

    @Test
    fun `amazon shopping ranks uncapped 5 percent card above partially capped card on large spend`() {
        val cards = SampleDataFactory.createSampleCards("test_uid").map { it.second }
        val transactions = SampleDataFactory.createSampleTransactions("test_uid").map { it.second }

        val ranked = BenefitRankingEngine.rankCards(
            cards = cards,
            transactions = transactions,
            query = PurchaseSearchQuery(
                merchantText = "Amazon",
                category = SpendCategory.SHOPPING,
                amount = 30_000.0,
                paymentMode = PaymentMode.ONLINE,
                billingCycleKey = currentBillingCycleKey()
            )
        )

        // Only the 3 cards with "amazon" keyword are returned (Axis ACE does not match)
        assertEquals(3, ranked.size)
        assertEquals(1500.0, ranked.first().estimatedBenefitAmount, 0.01)
        val hdfcRec = ranked.first { it.card.id == SampleDataFactory.SAMPLE_CARD_HDFC_ID }
        // HDFC Millennia had 550 already used out of 1,000 monthly cap -> 450 remaining
        assertEquals(450.0, hdfcRec.estimatedBenefitAmount, 0.01)
        assertEquals(450.0, hdfcRec.remainingCapInCycle ?: -1.0, 0.01)
    }

    @Test
    fun `exhausted monthly cap zeroes matched rule return and surfaces cap warning`() {
        val cards = SampleDataFactory.createSampleCards("test_uid").map { it.second }
        val cycle = currentBillingCycleKey()
        val exhaustedTx = listOf(
            PurchaseLog(
                id = "tx_full",
                userId = "test_uid",
                cardId = SampleDataFactory.SAMPLE_CARD_AXIS_ID,
                merchantName = "Electricity",
                category = SpendCategory.UTILITIES.name,
                paymentMode = PaymentMode.PORTAL.name,
                amount = 10_000.0,
                matchedRuleId = "axis_ace_utilities_5pct",
                expectedBenefitAmount = 500.0,
                billingCycleKey = cycle
            )
        )

        val ranked = BenefitRankingEngine.rankCards(
            cards = cards,
            transactions = exhaustedTx,
            query = PurchaseSearchQuery(
                merchantText = "Electricity",
                category = SpendCategory.UTILITIES,
                amount = 5_000.0,
                paymentMode = PaymentMode.PORTAL,
                billingCycleKey = cycle
            )
        )

        val axisRec = ranked.first { it.card.id == SampleDataFactory.SAMPLE_CARD_AXIS_ID }
        assertTrue(axisRec.capReached)
        assertEquals(0.0, axisRec.estimatedBenefitAmount, 0.01)
        assertTrue(axisRec.warnings.any { it.contains("exhausted", ignoreCase = true) })
    }

    @Test
    fun `emi payment mode respects emi exclusion warnings`() {
        val cards = SampleDataFactory.createSampleCards("test_uid").map { it.second }
        val ranked = BenefitRankingEngine.rankCards(
            cards = cards,
            transactions = emptyList(),
            query = PurchaseSearchQuery(
                merchantText = "Amazon",
                category = SpendCategory.SHOPPING,
                amount = 20_000.0,
                paymentMode = PaymentMode.EMI,
                billingCycleKey = currentBillingCycleKey()
            )
        )

        val iciciRec = ranked.first { it.card.id == SampleDataFactory.SAMPLE_CARD_AMAZON_ID }
        assertTrue(iciciRec.warnings.any { it.contains("EMI", ignoreCase = true) })
    }

    @Test
    fun `fuel waiver enforces min and max fuel transaction ceiling`() {
        val cards = SampleDataFactory.createSampleCards("test_uid").map { it.second }

        val eligibleFuel = BenefitRankingEngine.rankCards(
            cards = cards,
            transactions = emptyList(),
            query = PurchaseSearchQuery(
                merchantText = "IndianOil",
                category = SpendCategory.FUEL,
                amount = 2_000.0,
                paymentMode = PaymentMode.IN_STORE
            )
        )
        assertEquals(1, eligibleFuel.size)
        assertEquals(20.0, eligibleFuel.first().estimatedBenefitAmount, 0.01)
        assertTrue(eligibleFuel.first().warnings.isEmpty())

        // ₹5,000 exceeds maxTxnAmount of ₹4,000 -> 0 benefit + ceiling warning
        val overCeilingFuel = BenefitRankingEngine.rankCards(
            cards = cards,
            transactions = emptyList(),
            query = PurchaseSearchQuery(
                merchantText = "IndianOil",
                category = SpendCategory.FUEL,
                amount = 5_000.0,
                paymentMode = PaymentMode.IN_STORE
            )
        )
        assertEquals(1, overCeilingFuel.size)
        assertEquals(0.0, overCeilingFuel.first().estimatedBenefitAmount, 0.01)
        assertTrue(overCeilingFuel.first().warnings.any { it.contains("exceeded", ignoreCase = true) })
    }

    @Test
    fun `dining offer matches coupon code and exposes coupon on recommendation`() {
        val cardWithCoupon = CreditCard(
            id = "card_coupon",
            userId = "test_uid",
            issuer = "HDFC Bank",
            cardName = "Diners Privilege",
            lastFour = "9999",
            rules = listOf(
                CardBenefitRule(
                    ruleId = "dine_coupon_1",
                    title = "20% Off Swiggy Dineout",
                    ruleType = RuleType.DINING.name,
                    category = SpendCategory.DINING.name,
                    merchantKeywords = listOf("swiggy", "dineout"),
                    benefitPercent = 20.0,
                    minSpend = 1_500.0,
                    monthlyCapAmount = 500.0,
                    benefitPeriod = BenefitPeriod.PER_TXN.name,
                    couponCode = "HDFCDINE20"
                )
            )
        )

        val rankedByCoupon = BenefitRankingEngine.rankCards(
            cards = listOf(cardWithCoupon),
            transactions = emptyList(),
            query = PurchaseSearchQuery(
                merchantText = "HDFCDINE20",
                category = SpendCategory.DINING,
                amount = 2_000.0,
                paymentMode = PaymentMode.ONLINE
            )
        )

        assertEquals(1, rankedByCoupon.size)
        val rec = rankedByCoupon.first()
        assertEquals("HDFCDINE20", rec.couponCode)
        assertEquals(400.0, rec.estimatedBenefitAmount, 0.01)
        assertFalse(rec.minSpendNotMet)
    }

    @Test
    fun `milestone rule builds structured auto summary with period and reward type`() {
        val milestoneRule = CardBenefitRule(
            ruleId = "ms_summary",
            title = "Annual Flight Milestone",
            ruleType = RuleType.MILESTONE.name,
            category = SpendCategory.ALL.name,
            minSpend = 400_000.0,
            benefitPeriod = BenefitPeriod.ANNUAL.name,
            milestoneRewardType = MilestoneRewardType.AIR_MILES.name,
            milestoneRewardDetail = "10,000 Bonus Air Miles",
            merchantKeywords = listOf("milestone", "miles")
        )

        val summary = BenefitRankingEngine.buildAutoConditionsSummary(milestoneRule, "₹")
        assertTrue(summary.contains("annual", ignoreCase = true))
        assertTrue(summary.contains("Air Miles", ignoreCase = true))
        assertTrue(summary.contains("10,000 Bonus Air Miles", ignoreCase = true))
    }
}
