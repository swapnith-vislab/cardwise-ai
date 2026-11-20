package com.example.model.india

import com.example.model.BankCatalogEntry
import com.example.model.CardNetwork
import com.example.model.CardTheme
import com.example.model.CreditCardCatalogItem
import com.example.model.RewardType
import java.util.Locale

/**
 * Compact, type-safe builders for the 1,000+ Indian credit card & card product catalog.
 * Spec format per card: "Card Name|NET|THEME|BASE_RATE|REWARD"
 *  - NET: V=VISA, M=MASTERCARD, R=RUPAY, A=AMEX, D=DINERS
 *  - THEME: O=OBSIDIAN, E=EMERALD, S=SAPPHIRE, G=GOLD, C=CRIMSON, L=SLATE
 *  - REWARD: C=CASHBACK, P=POINTS, M=MILES
 */
internal fun inBank(
    bankName: String,
    officialWebsiteUrl: String,
    officialDomain: String,
    vararg cardSpecs: String
): BankCatalogEntry {
    val parsedCards = cardSpecs.mapNotNull { raw ->
        val parts = raw.split("|")
        val name = parts.getOrNull(0)?.trim().orEmpty()
        if (name.isBlank()) return@mapNotNull null

        val network = when (parts.getOrNull(1)?.trim()?.uppercase(Locale.ROOT)) {
            "M" -> CardNetwork.MASTERCARD
            "R" -> CardNetwork.RUPAY
            "A" -> CardNetwork.AMEX
            "D" -> CardNetwork.DINERS
            else -> CardNetwork.VISA
        }
        val theme = when (parts.getOrNull(2)?.trim()?.uppercase(Locale.ROOT)) {
            "E" -> CardTheme.EMERALD
            "S" -> CardTheme.SAPPHIRE
            "G" -> CardTheme.GOLD
            "C" -> CardTheme.CRIMSON
            "L" -> CardTheme.SLATE
            else -> CardTheme.OBSIDIAN
        }
        val baseRate = parts.getOrNull(3)?.trim()?.toDoubleOrNull() ?: 0.5
        val rewardType = when (parts.getOrNull(4)?.trim()?.uppercase(Locale.ROOT)) {
            "C" -> RewardType.CASHBACK
            "M" -> RewardType.MILES
            else -> RewardType.POINTS
        }
        CreditCardCatalogItem(
            cardName = name,
            defaultNetwork = network,
            defaultTheme = theme,
            defaultBaseRatePercent = baseRate,
            defaultRewardType = rewardType
        )
    }.distinctBy { it.cardName.lowercase(Locale.ROOT) }

    return BankCatalogEntry(
        bankName = bankName,
        countryCode = "IN",
        countryName = "India",
        officialWebsiteUrl = officialWebsiteUrl,
        officialDomain = officialDomain,
        currencySymbol = "₹",
        cards = parsedCards
    )
}

object IndianCardCatalog {
    val banks: List<BankCatalogEntry> by lazy {
        IndianBanksPrivateMajor.banks +
            IndianBanksPrivateMidAndForeign.banks +
            IndianBanksPsuRegionalSfbFintech.banks +
            IndianBanksSpecializedAndCoBrand.banks
    }
}
