package com.example

import com.example.data.GlobalCardCatalogRepository
import com.example.model.BankCardCatalog
import com.example.model.BenefitPeriod
import com.example.model.CardBenefitRule
import com.example.model.MilestoneRewardType
import com.example.model.RuleType
import com.example.model.SpendCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalCatalogAndSyncTest {

    @Test
    fun worldwideCatalogCoversAtLeast20CountriesWithUniqueBanksAndValidUrls() {
        val summaries = BankCardCatalog.buildCountrySummaries()
        // Includes ALL_COUNTRIES + at least 20 distinct countries
        val countryOnly = summaries.filterNot { it.countryCode == BankCardCatalog.ALL_COUNTRIES_CODE }
        assertTrue("Expected at least 20 countries, found ${countryOnly.size}", countryOnly.size >= 20)
        assertTrue("Expected at least 60 global banks, found ${BankCardCatalog.banks.size}", BankCardCatalog.banks.size >= 60)

        val totalCards = BankCardCatalog.banks.sumOf { it.cards.size }
        assertTrue("Expected at least 200 global cards, found $totalCards", totalCards >= 200)

        for (bank in BankCardCatalog.banks) {
            assertEquals("Country code must be 2 uppercase letters for ${bank.bankName}", 2, bank.countryCode.length)
            assertTrue("Country name must not be blank for ${bank.bankName}", bank.countryName.isNotBlank())
            assertTrue("Currency symbol must not be blank for ${bank.bankName}", bank.currencySymbol.isNotBlank())
            assertTrue("Official URL must be HTTPS for ${bank.bankName}", bank.officialWebsiteUrl.startsWith("https://"))
            assertTrue("Official domain must not be blank for ${bank.bankName}", bank.officialDomain.isNotBlank())
        }
    }

    @Test
    fun indianCatalogHasMoreThanOneThousandUniqueProductsAcrossIndianBanks() {
        val indianBanks = BankCardCatalog.banks.filter { it.countryCode == "IN" }
        val totalIndianCards = indianBanks.sumOf { it.cards.size }

        assertTrue("Expected at least 50 Indian banks/issuers, found ${indianBanks.size}", indianBanks.size >= 50)
        assertTrue("Expected more than 1,000 Indian card products, found $totalIndianCards", totalIndianCards > 1000)

        val uniqueBankNames = indianBanks.map { it.bankName.lowercase() }.distinct()
        assertEquals("Indian bank names must be unique", indianBanks.size, uniqueBankNames.size)

        for (bank in indianBanks) {
            assertEquals("₹", bank.currencySymbol)
            assertEquals("India", bank.countryName)
            val distinctCardNames = bank.cards.map { it.cardName.lowercase() }.distinct()
            assertEquals("Duplicate card detected in ${bank.bankName}", bank.cards.size, distinctCardNames.size)
        }
    }

    @Test
    fun cardNameNormalizationDeduplicatesBankPrefixAndCreditCardSuffix() {
        val a = GlobalCardCatalogRepository.normalizeCardNameForComparison("HDFC Bank Regalia Gold Credit Card", "HDFC Bank")
        val b = GlobalCardCatalogRepository.normalizeCardNameForComparison("Regalia Gold", "HDFC Bank")
        assertEquals(a, b)
    }

    @Test
    fun structuredBenefitRuleSerializesDynamicFieldsToFirestoreMap() {
        val milestoneRule = CardBenefitRule(
            ruleId = "ms_1",
            title = "Quarterly Movie Milestone",
            ruleType = RuleType.MILESTONE.name,
            category = SpendCategory.ALL.name,
            minSpend = 100_000.0,
            flatBenefitAmount = 1_500.0,
            benefitPeriod = BenefitPeriod.QUARTERLY.name,
            milestoneRewardType = MilestoneRewardType.MOVIE_TICKETS.name,
            milestoneRewardDetail = "2 Free BookMyShow IMAX Tickets",
            merchantKeywords = listOf("milestone", "movie", "bookmyshow")
        )
        val milestoneMap = milestoneRule.toMap()
        assertEquals(BenefitPeriod.QUARTERLY.name, milestoneMap["benefitPeriod"])
        assertEquals(MilestoneRewardType.MOVIE_TICKETS.name, milestoneMap["milestoneRewardType"])
        assertEquals("2 Free BookMyShow IMAX Tickets", milestoneMap["milestoneRewardDetail"])

        val fuelRule = CardBenefitRule(
            ruleId = "fuel_1",
            title = "1% Fuel Surcharge Waiver",
            ruleType = RuleType.FUEL.name,
            category = SpendCategory.FUEL.name,
            benefitPercent = 1.0,
            minSpend = 400.0,
            maxTxnAmount = 4_000.0,
            monthlyCapAmount = 250.0,
            benefitPeriod = BenefitPeriod.MONTHLY.name,
            merchantKeywords = listOf("fuel", "shell", "indianoil")
        )
        val fuelMap = fuelRule.toMap()
        assertEquals(400.0, fuelMap["minSpend"])
        assertEquals(4_000.0, fuelMap["maxTxnAmount"])
        assertEquals(250.0, fuelMap["monthlyCapAmount"])
        assertEquals(BenefitPeriod.MONTHLY.name, fuelMap["benefitPeriod"])

        val diningRule = CardBenefitRule(
            ruleId = "dine_1",
            title = "20% Off Zomato Dining",
            ruleType = RuleType.DINING.name,
            category = SpendCategory.DINING.name,
            benefitPercent = 20.0,
            minSpend = 1_500.0,
            monthlyCapAmount = 500.0,
            benefitPeriod = BenefitPeriod.PER_TXN.name,
            couponCode = "DINE20OFF",
            merchantKeywords = listOf("zomato", "dining")
        )
        val diningMap = diningRule.toMap()
        assertEquals("DINE20OFF", diningMap["couponCode"])
        assertEquals(BenefitPeriod.PER_TXN.name, diningMap["benefitPeriod"])

        val loungeRule = CardBenefitRule(
            ruleId = "lounge_1",
            title = "Domestic Lounge Access",
            ruleType = RuleType.LOUNGE.name,
            category = SpendCategory.TRAVEL.name,
            loungeVisitsCount = 4,
            benefitPeriod = BenefitPeriod.QUARTERLY.name,
            merchantKeywords = listOf("lounge", "airport")
        )
        val loungeMap = loungeRule.toMap()
        assertEquals(4, loungeMap["loungeVisitsCount"])
        assertEquals(BenefitPeriod.QUARTERLY.name, loungeMap["benefitPeriod"])
    }
}
