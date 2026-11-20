package com.example.data

import com.example.data.local.CatalogBankEntity
import com.example.data.local.CatalogCardEntity
import com.example.data.local.CatalogSyncStateEntity
import com.example.data.local.GlobalCatalogDao
import com.example.model.BankCardCatalog
import com.example.model.BankCatalogEntry
import com.example.model.CardNetwork
import com.example.model.CardTheme
import com.example.model.CreditCardCatalogItem
import com.example.model.RewardType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Locale

/**
 * Coordinates the offline-first local Room SQLite worldwide credit card directory:
 * 1) Pre-bundled worldwide bank/card seed data (22 countries, 60+ banks, 240+ cards).
 * 2) Country/market filtering persisted in Room.
 * 3) Free local persistence of custom banks & cards added by the user (zero AI quota required).
 */
class GlobalCardCatalogRepository(
    private val dao: GlobalCatalogDao
) {
    val banksFlow: Flow<List<BankCatalogEntry>> = combine(
        dao.observeAllBanks(),
        dao.observeAllCards()
    ) { bankEntities, cardEntities ->
        val cardsByBankKey = cardEntities.groupBy { it.bankKey }
        bankEntities.map { bank ->
            val cards = cardsByBankKey[bank.bankKey].orEmpty().map { card ->
                CreditCardCatalogItem(
                    cardName = card.cardName,
                    defaultNetwork = CardNetwork.fromRaw(card.defaultNetwork),
                    defaultTheme = CardTheme.fromRaw(card.defaultTheme),
                    defaultBaseRatePercent = card.defaultBaseRatePercent,
                    defaultRewardType = RewardType.fromRaw(card.defaultRewardType),
                    isNewlyDiscovered = false,
                    sourceUrl = card.sourceUrl
                )
            }
            BankCatalogEntry(
                bankName = bank.bankName,
                countryCode = bank.countryCode,
                countryName = bank.countryName,
                officialWebsiteUrl = bank.officialWebsiteUrl,
                officialDomain = bank.officialDomain,
                currencySymbol = bank.currencySymbol,
                cards = cards,
                isUserDiscovered = bank.isUserDiscovered
            )
        }
    }

    val selectedCountryCodeFlow: Flow<String> = dao.observeSyncState().map { existing ->
        existing?.selectedCountryCode?.ifBlank { "IN" } ?: "IN"
    }

    /**
     * Seeds all pre-bundled worldwide banks & cards idempotently using INSERT OR IGNORE so
     * user-added custom banks and cards are always preserved.
     */
    suspend fun seedCatalogIfNeeded() {
        val now = System.currentTimeMillis()
        val bankEntities = mutableListOf<CatalogBankEntity>()
        val cardEntities = mutableListOf<CatalogCardEntity>()

        for (bank in BankCardCatalog.banks) {
            val bankKey = BankCardCatalog.normalizeKey(bank.countryCode, bank.bankName)
            bankEntities.add(
                CatalogBankEntity(
                    bankKey = bankKey,
                    bankName = bank.bankName,
                    countryCode = bank.countryCode.uppercase(Locale.ROOT),
                    countryName = bank.countryName,
                    currencySymbol = bank.currencySymbol,
                    officialWebsiteUrl = bank.officialWebsiteUrl,
                    officialDomain = bank.officialDomain,
                    isUserDiscovered = false,
                    addedAtEpochMs = now
                )
            )
            for (card in bank.cards) {
                val cardKey = BankCardCatalog.normalizeKey(bankKey, card.cardName)
                cardEntities.add(
                    CatalogCardEntity(
                        cardKey = cardKey,
                        bankKey = bankKey,
                        bankName = bank.bankName,
                        countryCode = bank.countryCode.uppercase(Locale.ROOT),
                        cardName = card.cardName,
                        defaultNetwork = card.defaultNetwork.name,
                        defaultTheme = card.defaultTheme.name,
                        defaultBaseRatePercent = card.defaultBaseRatePercent,
                        defaultRewardType = card.defaultRewardType.name,
                        isNewlyDiscovered = false,
                        discoveredAtEpochMs = 0L,
                        sourceUrl = bank.officialWebsiteUrl
                    )
                )
            }
        }

        bankEntities.chunked(150).forEach { batch ->
            dao.insertBanksIgnore(batch)
        }
        cardEntities.chunked(250).forEach { batch ->
            dao.insertCardsIgnore(batch)
        }

        if (dao.getSyncStateOnce() == null) {
            dao.upsertSyncState(
                CatalogSyncStateEntity(
                    selectedCountryCode = "IN",
                    lastSyncEpochMs = 0L,
                    lastSyncStatus = "OFFLINE_READY",
                    lastSyncMessage = "India 1,000+ & global card directory ready",
                    lastNewCardsFoundCount = 0,
                    nextScheduledEpochMs = 0L
                )
            )
        }
    }

    suspend fun updateSelectedCountry(countryCode: String) {
        val cleanCode = countryCode.trim().uppercase(Locale.ROOT).ifBlank { BankCardCatalog.ALL_COUNTRIES_CODE }
        val current = dao.getSyncStateOnce() ?: CatalogSyncStateEntity()
        dao.upsertSyncState(current.copy(selectedCountryCode = cleanCode))
    }

    /**
     * Saves a user-entered custom bank and/or card into the local Room catalog so it becomes
     * immediately selectable in all country, bank, and card dropdowns.
     */
    suspend fun saveCustomBankAndCard(
        countryCode: String,
        countryName: String,
        currencySymbol: String,
        bankName: String,
        cardName: String,
        network: CardNetwork = CardNetwork.VISA,
        cardTheme: CardTheme = CardTheme.OBSIDIAN,
        baseRatePercent: Double = 1.0,
        rewardType: RewardType = RewardType.CASHBACK,
        officialWebsiteUrl: String = ""
    ): Boolean {
        val cleanBank = bankName.trim()
        val cleanCard = cardName.trim()
        if (cleanBank.isBlank() || cleanCard.isBlank()) return false

        val now = System.currentTimeMillis()
        val allExistingBanks = dao.getAllBanksOnce()
        val existingBank = allExistingBanks.firstOrNull {
            it.bankName.equals(cleanBank, ignoreCase = true)
        }

        val resolvedCountryCode = existingBank?.countryCode
            ?: countryCode.trim().uppercase(Locale.ROOT)
                .takeIf { it.length == 2 && it != BankCardCatalog.ALL_COUNTRIES_CODE }
            ?: "IN"
        val resolvedCountryName = existingBank?.countryName
            ?: countryName.trim().ifBlank {
                BankCardCatalog.buildCountrySummaries()
                    .firstOrNull { it.countryCode == resolvedCountryCode }?.countryName
                    ?: resolvedCountryCode
            }
        val resolvedCurrency = existingBank?.currencySymbol ?: currencySymbol.trim().ifBlank { "₹" }
        val resolvedBankKey = existingBank?.bankKey ?: BankCardCatalog.normalizeKey(resolvedCountryCode, cleanBank)
        val resolvedBankName = existingBank?.bankName ?: cleanBank

        val safeUrl = officialWebsiteUrl.trim().takeIf {
            it.startsWith("https://", ignoreCase = true) || it.startsWith("http://", ignoreCase = true)
        }.orEmpty()
        val domain = safeUrl.removePrefix("https://").removePrefix("http://").substringBefore("/").removePrefix("www.")

        if (existingBank == null) {
            dao.upsertBank(
                CatalogBankEntity(
                    bankKey = resolvedBankKey,
                    bankName = resolvedBankName,
                    countryCode = resolvedCountryCode,
                    countryName = resolvedCountryName,
                    currencySymbol = resolvedCurrency,
                    officialWebsiteUrl = safeUrl,
                    officialDomain = domain.ifBlank { resolvedBankName },
                    isUserDiscovered = true,
                    addedAtEpochMs = now
                )
            )
        }

        val cardKey = BankCardCatalog.normalizeKey(resolvedBankKey, cleanCard)
        val existingBankCardNames = dao.getCardsForBankOnce(resolvedBankKey)
            .map { normalizeCardNameForComparison(it.cardName, resolvedBankName) }
            .toSet()

        if (normalizeCardNameForComparison(cleanCard, resolvedBankName) in existingBankCardNames) {
            return false
        }

        dao.insertCardsIgnore(
            listOf(
                CatalogCardEntity(
                    cardKey = cardKey,
                    bankKey = resolvedBankKey,
                    bankName = resolvedBankName,
                    countryCode = resolvedCountryCode,
                    cardName = cleanCard,
                    defaultNetwork = network.name,
                    defaultTheme = cardTheme.name,
                    defaultBaseRatePercent = baseRatePercent.coerceIn(0.0, 50.0),
                    defaultRewardType = rewardType.name,
                    isNewlyDiscovered = false,
                    discoveredAtEpochMs = now,
                    sourceUrl = safeUrl
                )
            )
        )
        return true
    }

    companion object {
        internal fun normalizeCardNameForComparison(cardName: String, bankName: String): String {
            val stripped = cardName
                .lowercase(Locale.ROOT)
                .replace(bankName.lowercase(Locale.ROOT), "")
                .replace("credit card", "")
                .replace("card", "")
                .replace(Regex("[^a-z0-9]+"), "")
            return stripped.ifBlank {
                cardName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "")
            }
        }
    }
}
