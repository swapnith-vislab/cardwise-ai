package com.example.model

import com.example.model.india.IndianCardCatalog
import java.util.Locale

data class CreditCardCatalogItem(
    val cardName: String,
    val defaultNetwork: CardNetwork,
    val defaultTheme: CardTheme,
    val defaultBaseRatePercent: Double,
    val defaultRewardType: RewardType,
    val isNewlyDiscovered: Boolean = false,
    val sourceUrl: String = ""
)

data class BankCatalogEntry(
    val bankName: String,
    val countryCode: String,
    val countryName: String,
    val officialWebsiteUrl: String,
    val officialDomain: String,
    val currencySymbol: String,
    val cards: List<CreditCardCatalogItem>,
    val isUserDiscovered: Boolean = false
) {
    /** Backward-compatible display label for the bank's market/region. */
    val region: String get() = countryName
}

data class CountryCatalogSummary(
    val countryCode: String,
    val countryName: String,
    val currencySymbol: String,
    val bankCount: Int,
    val cardCount: Int
)

/**
 * Comprehensive credit card catalog focused on India (1,100+ Indian credit cards & card products
 * across 70+ Indian banks, PSU/SFB/Co-op issuers, and fintech co-brands) plus 20+ global markets.
 * Seeded into Room SQLite at startup and locally extensible with user-added custom banks and cards.
 */
object BankCardCatalog {

    const val ALL_COUNTRIES_CODE = "ALL"
    const val ALL_COUNTRIES_NAME = "All Countries (Worldwide)"
    const val CUSTOM_BANK_OPTION = "Other / Custom Bank"
    const val CUSTOM_CARD_OPTION = "Other / Custom Card"

    private val usCoreBanks: List<BankCatalogEntry> = listOf(
        // --- United States (US) ---
        BankCatalogEntry(
            bankName = "Chase",
            countryCode = "US",
            countryName = "United States",
            officialWebsiteUrl = "https://creditcards.chase.com/",
            officialDomain = "chase.com",
            currencySymbol = "$",
            cards = listOf(
                CreditCardCatalogItem("Sapphire Preferred", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Sapphire Reserve", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Freedom Unlimited", CardNetwork.VISA, CardTheme.SLATE, 1.5, RewardType.CASHBACK),
                CreditCardCatalogItem("Freedom Flex", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Prime Visa", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Ink Business Preferred", CardNetwork.VISA, CardTheme.EMERALD, 1.0, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "American Express US",
            countryCode = "US",
            countryName = "United States",
            officialWebsiteUrl = "https://www.americanexpress.com/us/credit-cards/",
            officialDomain = "americanexpress.com",
            currencySymbol = "$",
            cards = listOf(
                CreditCardCatalogItem("Gold Card", CardNetwork.AMEX, CardTheme.GOLD, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Platinum Card", CardNetwork.AMEX, CardTheme.SLATE, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Blue Cash Preferred", CardNetwork.AMEX, CardTheme.SAPPHIRE, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Blue Cash Everyday", CardNetwork.AMEX, CardTheme.EMERALD, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Green Card", CardNetwork.AMEX, CardTheme.EMERALD, 1.0, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Capital One",
            countryCode = "US",
            countryName = "United States",
            officialWebsiteUrl = "https://www.capitalone.com/credit-cards/",
            officialDomain = "capitalone.com",
            currencySymbol = "$",
            cards = listOf(
                CreditCardCatalogItem("Venture X", CardNetwork.VISA, CardTheme.OBSIDIAN, 2.0, RewardType.MILES),
                CreditCardCatalogItem("Venture Rewards", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 2.0, RewardType.MILES),
                CreditCardCatalogItem("Savor Cash Rewards", CardNetwork.MASTERCARD, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Quicksilver", CardNetwork.MASTERCARD, CardTheme.SLATE, 1.5, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "Citi US",
            countryCode = "US",
            countryName = "United States",
            officialWebsiteUrl = "https://www.citi.com/credit-cards/compare/view-all-credit-cards",
            officialDomain = "citi.com",
            currencySymbol = "$",
            cards = listOf(
                CreditCardCatalogItem("Double Cash", CardNetwork.MASTERCARD, CardTheme.EMERALD, 2.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Custom Cash", CardNetwork.MASTERCARD, CardTheme.SAPPHIRE, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Strata Premier", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Costco Anywhere Visa", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "Bank of America",
            countryCode = "US",
            countryName = "United States",
            officialWebsiteUrl = "https://www.bankofamerica.com/credit-cards/",
            officialDomain = "bankofamerica.com",
            currencySymbol = "$",
            cards = listOf(
                CreditCardCatalogItem("Customized Cash Rewards", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Unlimited Cash Rewards", CardNetwork.VISA, CardTheme.SLATE, 1.5, RewardType.CASHBACK),
                CreditCardCatalogItem("Premium Rewards", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.5, RewardType.POINTS),
                CreditCardCatalogItem("Travel Rewards", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.5, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Wells Fargo",
            countryCode = "US",
            countryName = "United States",
            officialWebsiteUrl = "https://creditcards.wellsfargo.com/",
            officialDomain = "wellsfargo.com",
            currencySymbol = "$",
            cards = listOf(
                CreditCardCatalogItem("Autograph", CardNetwork.VISA, CardTheme.CRIMSON, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Autograph Journey", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Active Cash", CardNetwork.VISA, CardTheme.EMERALD, 2.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Bilt Mastercard", CardNetwork.MASTERCARD, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS)
            )
        ),
        BankCatalogEntry(
            bankName = "Discover",
            countryCode = "US",
            countryName = "United States",
            officialWebsiteUrl = "https://www.discover.com/credit-cards/",
            officialDomain = "discover.com",
            currencySymbol = "$",
            cards = listOf(
                CreditCardCatalogItem("Discover it Cash Back", CardNetwork.DISCOVER, CardTheme.SAPPHIRE, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Discover it Miles", CardNetwork.DISCOVER, CardTheme.SLATE, 1.5, RewardType.MILES),
                CreditCardCatalogItem("Discover it Chrome", CardNetwork.DISCOVER, CardTheme.OBSIDIAN, 1.0, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "U.S. Bank",
            countryCode = "US",
            countryName = "United States",
            officialWebsiteUrl = "https://www.usbank.com/credit-cards.html",
            officialDomain = "usbank.com",
            currencySymbol = "$",
            cards = listOf(
                CreditCardCatalogItem("Altitude Reserve", CardNetwork.VISA, CardTheme.OBSIDIAN, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Altitude Go", CardNetwork.VISA, CardTheme.SAPPHIRE, 1.0, RewardType.POINTS),
                CreditCardCatalogItem("Cash+ Visa Signature", CardNetwork.VISA, CardTheme.EMERALD, 1.0, RewardType.CASHBACK),
                CreditCardCatalogItem("Smartly Visa Signature", CardNetwork.VISA, CardTheme.SLATE, 2.0, RewardType.CASHBACK)
            )
        ),
        BankCatalogEntry(
            bankName = "Apple Card (Goldman Sachs)",
            countryCode = "US",
            countryName = "United States",
            officialWebsiteUrl = "https://www.apple.com/apple-card/",
            officialDomain = "apple.com",
            currencySymbol = "$",
            cards = listOf(
                CreditCardCatalogItem("Apple Card", CardNetwork.MASTERCARD, CardTheme.SLATE, 2.0, RewardType.CASHBACK)
            )
        )
    )

    val banks: List<BankCatalogEntry> = IndianCardCatalog.banks + usCoreBanks + GlobalBankSeeds.internationalBanks

    fun normalizeKey(vararg parts: String): String {
        return parts.joinToString("__") { part ->
            part.trim()
                .lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
        }
    }

    fun buildCountrySummaries(bankList: List<BankCatalogEntry> = banks): List<CountryCatalogSummary> {
        val grouped = bankList.groupBy { it.countryCode.uppercase(Locale.ROOT) }
        val specificCountries = grouped.entries.map { (code, list) ->
            val first = list.first()
            CountryCatalogSummary(
                countryCode = code,
                countryName = first.countryName,
                currencySymbol = first.currencySymbol,
                bankCount = list.size,
                cardCount = list.sumOf { it.cards.size }
            )
        }.sortedWith(
            compareByDescending<CountryCatalogSummary> { it.countryCode == "IN" }
                .thenByDescending { it.countryCode == "US" }
                .thenBy { it.countryName }
        )

        val allSummary = CountryCatalogSummary(
            countryCode = ALL_COUNTRIES_CODE,
            countryName = ALL_COUNTRIES_NAME,
            currencySymbol = "",
            bankCount = bankList.size,
            cardCount = bankList.sumOf { it.cards.size }
        )
        return listOf(allSummary) + specificCountries
    }

    fun findBank(issuer: String, sourceBanks: List<BankCatalogEntry> = banks): BankCatalogEntry? {
        val clean = issuer.trim()
        if (clean.isBlank()) return null
        return sourceBanks.firstOrNull { it.bankName.equals(clean, ignoreCase = true) }
            ?: sourceBanks.firstOrNull {
                val bLower = it.bankName.lowercase(Locale.ROOT)
                val qLower = clean.lowercase(Locale.ROOT)
                bLower.contains(qLower) || qLower.contains(bLower)
            }
    }

    fun findCard(
        issuer: String,
        cardName: String,
        sourceBanks: List<BankCatalogEntry> = banks
    ): CreditCardCatalogItem? {
        val bank = findBank(issuer, sourceBanks) ?: return null
        val cleanCard = cardName.trim()
        if (cleanCard.isBlank()) return null
        return bank.cards.firstOrNull { it.cardName.equals(cleanCard, ignoreCase = true) }
    }

    fun getCardsForBank(
        bankName: String,
        sourceBanks: List<BankCatalogEntry> = banks
    ): List<CreditCardCatalogItem> {
        return findBank(bankName, sourceBanks)?.cards ?: emptyList()
    }

    fun getOfficialBankUrl(issuer: String, sourceBanks: List<BankCatalogEntry> = banks): String? =
        findBank(issuer, sourceBanks)?.officialWebsiteUrl

    fun getOfficialBankDomain(issuer: String, sourceBanks: List<BankCatalogEntry> = banks): String? =
        findBank(issuer, sourceBanks)?.officialDomain
}
