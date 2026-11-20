package com.example

import com.example.domain.BenefitRankingEngine
import com.example.domain.PurchaseSearchQuery
import com.example.model.BankCardCatalog
import com.example.model.PaymentMode
import com.example.model.SampleDataFactory
import com.example.model.SpendCategory
import com.example.model.isSafeWebUri
import com.example.model.sanitizeDecimalInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BankCardCatalogTest {

    @Test
    fun allBanksHaveValidOfficialUrlsAndUniqueCards() {
        assertTrue("Catalog should list at least 15 banks", BankCardCatalog.banks.size >= 15)
        val bankNames = BankCardCatalog.banks.map { it.bankName.lowercase() }
        assertEquals("Bank names must be unique", bankNames.size, bankNames.distinct().size)

        for (bank in BankCardCatalog.banks) {
            assertTrue("Official URL must be HTTPS for ${bank.bankName}", bank.officialWebsiteUrl.startsWith("https://"))
            assertTrue("Official domain must not be blank for ${bank.bankName}", bank.officialDomain.isNotBlank())
            assertTrue("Bank ${bank.bankName} must have at least 1 credit card", bank.cards.isNotEmpty())
            val cardNames = bank.cards.map { it.cardName.lowercase() }
            assertEquals("Card names must be unique within ${bank.bankName}", cardNames.size, cardNames.distinct().size)
        }
    }

    @Test
    fun sampleCardsMatchBankAndCardDropdownCatalog() {
        val sampleCards = SampleDataFactory.createSampleCards("test_uid").map { it.second }
        for (card in sampleCards) {
            val bank = BankCardCatalog.findBank(card.issuer)
            assertNotNull("Sample issuer ${card.issuer} should exist in BankCardCatalog", bank)
            val catalogCard = BankCardCatalog.findCard(card.issuer, card.cardName)
            assertNotNull("Sample card ${card.issuer} ${card.cardName} should exist in BankCardCatalog", catalogCard)
        }
    }

    @Test
    fun sanitizeDecimalInputRemovesDuplicateDotsAndNonDigits() {
        assertEquals("1250.75", sanitizeDecimalInput("1,250.75.99"))
        assertEquals("0.5", sanitizeDecimalInput(".5.2"))
        assertEquals("10000", sanitizeDecimalInput("abc10000xyz"))
        assertEquals("", sanitizeDecimalInput("abc"))
    }

    @Test
    fun isSafeWebUriRejectsUnsafeSchemes() {
        assertTrue(isSafeWebUri("https://www.hdfcbank.com/personal/pay/cards/credit-cards"))
        assertTrue(isSafeWebUri("http://example.com"))
        assertFalse(isSafeWebUri("javascript:alert(1)"))
        assertFalse(isSafeWebUri("file:///etc/passwd"))
        assertFalse(isSafeWebUri(""))
    }

    @Test
    fun singleLetterMerchantQueryDoesNotFalseMatchAmazonOffer() {
        val cards = SampleDataFactory.createSampleCards("test_uid").map { it.second }
        val ranked = BenefitRankingEngine.rankCards(
            cards = cards,
            transactions = emptyList(),
            query = PurchaseSearchQuery(
                merchantText = "a",
                category = SpendCategory.ALL,
                amount = 10_000.0,
                paymentMode = PaymentMode.ONLINE
            )
        )
        assertTrue(
            "Single character query 'a' must return empty list until >3 letters are entered",
            ranked.isEmpty()
        )
    }
}
