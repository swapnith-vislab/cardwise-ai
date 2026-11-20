package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CardWiseRepository
import com.example.data.GlobalCardCatalogRepository
import com.example.data.local.RecentMerchantSearchDao
import com.example.data.local.RecentMerchantSearchEntity
import com.example.domain.BenefitRankingEngine
import com.example.domain.CapUsageSummary
import com.example.domain.PurchaseSearchQuery
import com.example.domain.RankedCardRecommendation
import com.example.model.BankCardCatalog
import com.example.model.BankCatalogEntry
import com.example.model.CardBenefitRule
import com.example.model.CardNetwork
import com.example.model.CardTheme
import com.example.model.CountryCatalogSummary
import com.example.model.CreditCard
import com.example.model.PaymentMode
import com.example.model.PurchaseLog
import com.example.model.RewardType
import com.example.model.SpendCategory
import com.example.model.currentBillingCycleKey
import com.example.model.sanitizeDecimalInput
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

private const val VM_TAG = "CardWiseViewModel"
private const val STOP_TIMEOUT_MS = 5_000L

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class AdvisorUiState(
    val merchantQuery: String = "",
    val selectedCategory: SpendCategory = SpendCategory.SHOPPING,
    val amountInput: String = "10000",
    val selectedPaymentMode: PaymentMode = PaymentMode.ONLINE,
    val recentSearches: List<RecentMerchantSearchEntity> = emptyList(),
    val recommendations: List<RankedCardRecommendation> = emptyList(),
    val hasCardsInWallet: Boolean = false,
    val isQueryTooShort: Boolean = true,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class ActivitySummaryState(
    val transactions: List<PurchaseLog> = emptyList(),
    val totalSpentThisCycle: Double = 0.0,
    val totalSavedThisCycle: Double = 0.0,
    val averageReturnPercent: Double = 0.0,
    val capSummaries: List<CapUsageSummary> = emptyList(),
    val currencySymbol: String = "₹",
    val currentCycleKey: String = currentBillingCycleKey()
)

data class GlobalCatalogUiState(
    val allBanks: List<BankCatalogEntry> = BankCardCatalog.banks,
    val countries: List<CountryCatalogSummary> = BankCardCatalog.buildCountrySummaries(BankCardCatalog.banks),
    val selectedCountryCode: String = "IN"
) {
    val filteredBanks: List<BankCatalogEntry>
        get() = if (selectedCountryCode == BankCardCatalog.ALL_COUNTRIES_CODE) {
            allBanks
        } else {
            allBanks.filter { it.countryCode.equals(selectedCountryCode, ignoreCase = true) }
        }

    val totalCardCount: Int
        get() = allBanks.sumOf { it.cards.size }

    val selectedCountryName: String
        get() = countries.firstOrNull { it.countryCode.equals(selectedCountryCode, ignoreCase = true) }?.countryName
            ?: BankCardCatalog.ALL_COUNTRIES_NAME
}

class CardWiseViewModel(
    private val repository: CardWiseRepository,
    private val recentSearchDao: RecentMerchantSearchDao,
    private val catalogRepository: GlobalCardCatalogRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(
        PurchaseSearchQuery(
            merchantText = "",
            category = SpendCategory.SHOPPING,
            amount = 10000.0,
            paymentMode = PaymentMode.ONLINE
        )
    )
    private val _amountInput = MutableStateFlow("10000")

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val catalogState: StateFlow<GlobalCatalogUiState> = combine(
        catalogRepository.banksFlow,
        catalogRepository.selectedCountryCodeFlow
    ) { roomBanks, selectedCode ->
        val effectiveBanks = roomBanks.ifEmpty { BankCardCatalog.banks }
        GlobalCatalogUiState(
            allBanks = effectiveBanks,
            countries = BankCardCatalog.buildCountrySummaries(effectiveBanks),
            selectedCountryCode = selectedCode.ifBlank { "IN" }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = GlobalCatalogUiState()
    )

    val cardsState: StateFlow<UiState<List<CreditCard>>> = repository.observeCards(currentUserId)
        .map<List<CreditCard>, UiState<List<CreditCard>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(VM_TAG, "Error observing cards", rootError)
            emit(UiState.Error(rootError.message ?: "Unable to load your credit cards."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = UiState.Loading
        )

    val transactionsState: StateFlow<UiState<List<PurchaseLog>>> =
        repository.observeTransactions(currentUserId)
            .map<List<PurchaseLog>, UiState<List<PurchaseLog>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(VM_TAG, "Error observing transactions", rootError)
                emit(UiState.Error(rootError.message ?: "Unable to load savings activity."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    private val recentSearchesFlow: StateFlow<List<RecentMerchantSearchEntity>> =
        recentSearchDao.observeRecentSearches(currentUserId)
            .catch { err ->
                Log.w(VM_TAG, "Local SQL recent searches error: ${err.message}")
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = emptyList()
            )

    val advisorState: StateFlow<AdvisorUiState> = combine(
        cardsState,
        transactionsState,
        _searchQuery,
        _amountInput,
        recentSearchesFlow
    ) { cardsUi, txUi, query, amountRaw, recents ->
        val cards = (cardsUi as? UiState.Success)?.data ?: emptyList()
        val transactions = (txUi as? UiState.Success)?.data ?: emptyList()
        val isLoading = cardsUi is UiState.Loading
        val errorMsg = (cardsUi as? UiState.Error)?.message ?: (txUi as? UiState.Error)?.message
        val isQueryTooShort = query.merchantText.trim().length < BenefitRankingEngine.MIN_SEARCH_QUERY_LENGTH

        val ranked = if (cards.isNotEmpty() && !isQueryTooShort) {
            BenefitRankingEngine.rankCards(cards, transactions, query)
        } else {
            emptyList()
        }

        AdvisorUiState(
            merchantQuery = query.merchantText,
            selectedCategory = query.category,
            amountInput = amountRaw,
            selectedPaymentMode = query.paymentMode,
            recentSearches = recents,
            recommendations = ranked,
            hasCardsInWallet = cards.isNotEmpty(),
            isQueryTooShort = isQueryTooShort,
            isLoading = isLoading,
            errorMessage = errorMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = AdvisorUiState()
    )

    val activitySummaryState: StateFlow<ActivitySummaryState> = combine(
        cardsState,
        transactionsState
    ) { cardsUi, txUi ->
        val cards = (cardsUi as? UiState.Success)?.data ?: emptyList()
        val transactions = (txUi as? UiState.Success)?.data ?: emptyList()
        val cycleKey = currentBillingCycleKey()
        val cycleTransactions = transactions.filter { it.billingCycleKey == cycleKey }
        val totalSpent = cycleTransactions.sumOf { it.amount }
        val totalSaved = cycleTransactions.sumOf { it.expectedBenefitAmount }
        val avgReturn = if (totalSpent > 0.0) (totalSaved / totalSpent) * 100.0 else 0.0
        val currency = cards.firstOrNull()?.currencySymbol
            ?: transactions.firstOrNull()?.currencySymbol
            ?: "₹"

        ActivitySummaryState(
            transactions = transactions,
            totalSpentThisCycle = totalSpent,
            totalSavedThisCycle = totalSaved,
            averageReturnPercent = avgReturn,
            capSummaries = BenefitRankingEngine.buildActiveCapSummaries(cards, transactions, cycleKey),
            currencySymbol = currency,
            currentCycleKey = cycleKey
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ActivitySummaryState()
    )

    init {
        viewModelScope.launch {
            try {
                catalogRepository.seedCatalogIfNeeded()
            } catch (e: Exception) {
                Log.w(VM_TAG, "Global catalog Room seed warning: ${e.message}")
            }
        }
        viewModelScope.launch {
            repository.seedSampleDataIfNeeded(currentUserId).onFailure { err ->
                Log.w(VM_TAG, "Sample data check skipped or failed: ${err.message}")
            }
        }
    }

    fun selectCatalogCountry(countryCode: String) {
        viewModelScope.launch {
            try {
                catalogRepository.updateSelectedCountry(countryCode)
            } catch (e: Exception) {
                Log.w(VM_TAG, "Failed to update selected catalog country: ${e.message}")
            }
        }
    }

    fun saveCustomBankAndCardToCatalog(
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
    ) {
        viewModelScope.launch {
            try {
                val added = catalogRepository.saveCustomBankAndCard(
                    countryCode = countryCode,
                    countryName = countryName,
                    currencySymbol = currencySymbol,
                    bankName = bankName,
                    cardName = cardName,
                    network = network,
                    cardTheme = cardTheme,
                    baseRatePercent = baseRatePercent,
                    rewardType = rewardType,
                    officialWebsiteUrl = officialWebsiteUrl
                )
                if (added) {
                    _snackbarMessage.value = "Saved $bankName $cardName to your local card directory"
                } else {
                    _snackbarMessage.value = "$bankName $cardName is already in your card directory"
                }
            } catch (e: Exception) {
                Log.w(VM_TAG, "Failed to save custom bank/card to catalog", e)
                _snackbarMessage.value = "Could not save to local directory."
            }
        }
    }

    fun updateMerchantQuery(merchant: String) {
        _searchQuery.update { it.copy(merchantText = merchant) }
    }

    fun applyQuickMerchantPreset(
        merchant: String,
        category: SpendCategory,
        mode: PaymentMode = PaymentMode.ONLINE,
        amount: Double? = null
    ) {
        if (amount != null && amount > 0.0) {
            _amountInput.value = amount.toLong().toString()
        }
        _searchQuery.update {
            it.copy(
                merchantText = merchant,
                category = category,
                paymentMode = mode,
                amount = amount ?: it.amount
            )
        }
        saveRecentMerchantToLocalSql(merchant, category, mode, amount ?: _searchQuery.value.amount ?: 0.0)
    }

    fun updateCategory(category: SpendCategory) {
        _searchQuery.update { it.copy(category = category) }
    }

    fun updatePaymentMode(mode: PaymentMode) {
        _searchQuery.update { it.copy(paymentMode = mode) }
    }

    fun updateAmountInput(raw: String) {
        val cleaned = sanitizeDecimalInput(raw)
        _amountInput.value = cleaned
        val parsed = cleaned.toDoubleOrNull()?.takeIf { it > 0.0 }
        _searchQuery.update { it.copy(amount = parsed) }
    }

    private fun saveRecentMerchantToLocalSql(
        merchant: String,
        category: SpendCategory,
        mode: PaymentMode,
        amount: Double
    ) {
        val trimmed = merchant.trim()
        if (trimmed.length < BenefitRankingEngine.MIN_SEARCH_QUERY_LENGTH) return
        viewModelScope.launch {
            try {
                val key = "${currentUserId}_${trimmed.lowercase(Locale.ROOT)}"
                recentSearchDao.upsertSearch(
                    RecentMerchantSearchEntity(
                        merchantKey = key,
                        userId = currentUserId,
                        merchantLabel = trimmed,
                        categoryName = category.name,
                        paymentModeName = mode.name,
                        lastAmount = amount,
                        searchedAtEpochMs = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.w(VM_TAG, "Failed to persist recent search to Room SQLite: ${e.message}")
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun addCard(card: CreditCard) {
        viewModelScope.launch {
            val result = repository.addCard(card)
            result.fold(
                onSuccess = {
                    // Also save unlisted bank/card into local Room catalog for future dropdown convenience
                    try {
                        val state = catalogState.value
                        val countryCode = state.selectedCountryCode.takeIf { it != BankCardCatalog.ALL_COUNTRIES_CODE } ?: "IN"
                        catalogRepository.saveCustomBankAndCard(
                            countryCode = countryCode,
                            countryName = state.selectedCountryName,
                            currencySymbol = card.currencySymbol,
                            bankName = card.issuer,
                            cardName = card.cardName,
                            network = CardNetwork.fromRaw(card.network),
                            cardTheme = CardTheme.fromRaw(card.cardTheme),
                            baseRatePercent = card.baseRewardRatePercent,
                            rewardType = RewardType.fromRaw(card.rewardType)
                        )
                    } catch (e: Exception) {
                        Log.w(VM_TAG, "Optional local catalog merge skipped: ${e.message}")
                    }
                    _snackbarMessage.value = "${card.displayTitle} added to your wallet"
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed to add card."
                }
            )
        }
    }

    fun updateCard(card: CreditCard) {
        viewModelScope.launch {
            repository.updateCard(card).fold(
                onSuccess = {
                    _snackbarMessage.value = "Updated ${card.displayTitle}"
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed to update card."
                }
            )
        }
    }

    fun deleteCard(card: CreditCard) {
        viewModelScope.launch {
            repository.deleteCard(card.id).fold(
                onSuccess = {
                    _snackbarMessage.value = "Removed ${card.displayTitle}"
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed to remove card."
                }
            )
        }
    }

    fun saveRuleOverride(card: CreditCard, rule: CardBenefitRule) {
        viewModelScope.launch {
            val existingIndex = card.rules.indexOfFirst { it.ruleId == rule.ruleId && rule.ruleId.isNotBlank() }
            val updatedList = card.rules.toMutableList()
            val normalizedRule = if (rule.ruleId.isBlank()) {
                rule.copy(ruleId = "manual_${System.currentTimeMillis()}", aiGenerated = false)
            } else {
                rule.copy(aiGenerated = false)
            }
            if (existingIndex >= 0) {
                updatedList[existingIndex] = normalizedRule
            } else {
                updatedList.add(0, normalizedRule)
            }

            repository.updateCardRulesAndTerms(
                cardId = card.id,
                baseRewardRatePercent = card.baseRewardRatePercent,
                rewardType = RewardType.fromRaw(card.rewardType),
                pointValueInCurrency = card.pointValueInCurrency,
                aiFetched = false,
                aiSummary = card.aiSummary,
                aiLastUpdatedEpochMs = card.aiLastUpdatedEpochMs,
                rules = updatedList
            ).fold(
                onSuccess = {
                    _snackbarMessage.value = "Saved rule '${normalizedRule.title}'"
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Could not save rule."
                }
            )
        }
    }

    fun deleteRuleFromCard(card: CreditCard, ruleId: String) {
        viewModelScope.launch {
            val updatedList = card.rules.filterNot { it.ruleId == ruleId }
            repository.updateCardRulesAndTerms(
                cardId = card.id,
                baseRewardRatePercent = card.baseRewardRatePercent,
                rewardType = RewardType.fromRaw(card.rewardType),
                pointValueInCurrency = card.pointValueInCurrency,
                aiFetched = false,
                aiSummary = card.aiSummary,
                aiLastUpdatedEpochMs = card.aiLastUpdatedEpochMs,
                rules = updatedList
            ).fold(
                onSuccess = { _snackbarMessage.value = "Benefit rule removed" },
                onFailure = { err -> _snackbarMessage.value = err.localizedMessage ?: "Failed to remove rule." }
            )
        }
    }

    fun selectCardForPayment(
        recommendation: RankedCardRecommendation,
        merchantName: String,
        itemNote: String,
        finalAmount: Double
    ) {
        viewModelScope.launch {
            if (finalAmount <= 0.0) {
                _snackbarMessage.value = "Enter a valid purchase amount greater than 0."
                return@launch
            }
            val currentQuery = _searchQuery.value
            val card = recommendation.card
            val transactions = (transactionsState.value as? UiState.Success)?.data ?: emptyList()

            saveRecentMerchantToLocalSql(
                merchant = merchantName,
                category = currentQuery.category,
                mode = currentQuery.paymentMode,
                amount = finalAmount
            )

            val refreshed = BenefitRankingEngine.rankCards(
                cards = listOf(card),
                transactions = transactions,
                query = currentQuery.copy(
                    merchantText = merchantName,
                    amount = finalAmount
                )
            ).firstOrNull() ?: recommendation

            val log = PurchaseLog(
                userId = currentUserId,
                cardId = card.id,
                cardDisplayName = "${card.displayTitle} •• ${card.lastFour}",
                cardNetwork = card.network,
                merchantName = merchantName.trim().ifBlank { currentQuery.category.displayName },
                itemNote = itemNote.trim(),
                category = currentQuery.category.name,
                paymentMode = currentQuery.paymentMode.name,
                amount = finalAmount,
                currencySymbol = card.currencySymbol,
                matchedRuleId = refreshed.matchedRule?.ruleId ?: "",
                matchedRuleTitle = refreshed.headlineOffer,
                expectedBenefitAmount = refreshed.estimatedBenefitAmount,
                effectiveReturnPercent = refreshed.effectiveReturnPercent,
                billingCycleKey = currentBillingCycleKey(),
                sampleData = false
            )

            val newBalance = (card.statementBalance + finalAmount).coerceAtMost(100_000_000.0)
            repository.logPurchaseAndUpdateBalance(log, newBalance).fold(
                onSuccess = {
                    val savedText = "${card.currencySymbol}${String.format(Locale.US, "%.0f", refreshed.estimatedBenefitAmount)}"
                    _snackbarMessage.value =
                        "Logged with ${card.cardName}! Expected benefit: $savedText • Caps updated"
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed to record purchase."
                }
            )
        }
    }

    fun deleteTransaction(tx: PurchaseLog) {
        viewModelScope.launch {
            val cards = (cardsState.value as? UiState.Success)?.data ?: emptyList()
            val matchingCard = cards.firstOrNull { it.id == tx.cardId }
            val adjustedBalance = matchingCard?.let { (it.statementBalance - tx.amount).coerceAtLeast(0.0) }

            repository.deleteTransaction(
                transactionId = tx.id,
                cardIdForBalanceRollback = matchingCard?.id,
                adjustedStatementBalance = adjustedBalance
            ).fold(
                onSuccess = {
                    _snackbarMessage.value = "Removed purchase log, adjusted balance & restored monthly cap"
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed to delete log."
                }
            )
        }
    }

    fun clearSampleData() {
        viewModelScope.launch {
            val cards = (cardsState.value as? UiState.Success)?.data ?: emptyList()
            val txs = (transactionsState.value as? UiState.Success)?.data ?: emptyList()
            val sampleCardIds = cards.filter { it.sampleData }.map { it.id }
            val sampleTxIds = txs.filter { it.sampleData || it.cardId in sampleCardIds }.map { it.id }
            if (sampleCardIds.isEmpty() && sampleTxIds.isEmpty()) {
                _snackbarMessage.value = "No sample data remaining."
                return@launch
            }
            repository.clearSampleData(sampleCardIds, sampleTxIds).fold(
                onSuccess = {
                    _snackbarMessage.value = "Sample cards and demo logs cleared."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed to clear sample data."
                }
            )
        }
    }

    fun restoreSampleData() {
        viewModelScope.launch {
            repository.restoreSampleData().fold(
                onSuccess = {
                    _snackbarMessage.value = "Sample cards restored."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed to restore sample data."
                }
            )
        }
    }
}
