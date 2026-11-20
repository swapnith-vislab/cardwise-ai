package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.CardBenefitRule
import com.example.model.CreditCard
import com.example.model.formatCurrency
import com.example.model.formatPercent
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MetallicCreditCardView
import com.example.ui.theme.JetBrainsMono
import com.example.viewmodel.GlobalCatalogUiState
import com.example.viewmodel.UiState

private data class PrefilledCardTarget(
    val countryCode: String,
    val bankName: String,
    val cardName: String
)

@Composable
fun MyCardsScreen(
    cardsState: UiState<List<CreditCard>>,
    catalogState: GlobalCatalogUiState,
    onAddCard: (CreditCard) -> Unit,
    onUpdateCard: (CreditCard) -> Unit,
    onDeleteCard: (CreditCard) -> Unit,
    onSaveRule: (CreditCard, CardBenefitRule) -> Unit,
    onDeleteRule: (CreditCard, String) -> Unit,
    onClearSampleData: () -> Unit,
    onRestoreSampleData: () -> Unit,
    onSelectCatalogCountry: (String) -> Unit,
    onSaveCustomCatalogEntry: (
        countryCode: String,
        countryName: String,
        currencySymbol: String,
        bankName: String,
        cardName: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCardId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var prefillTarget by remember { mutableStateOf<PrefilledCardTarget?>(null) }
    var showGlobalExplorer by remember { mutableStateOf(false) }

    val cards = (cardsState as? UiState.Success)?.data ?: emptyList()
    val selectedCard = cards.firstOrNull { it.id == selectedCardId }

    if (selectedCard != null) {
        CardDetailScreen(
            card = selectedCard,
            catalogState = catalogState,
            onBack = { selectedCardId = null },
            onUpdateCard = onUpdateCard,
            onDeleteCard = { onDeleteCard(selectedCard) },
            onSaveRule = { rule -> onSaveRule(selectedCard, rule) },
            onDeleteRule = { ruleId -> onDeleteRule(selectedCard, ruleId) },
            modifier = modifier
        )
        return
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        when (cardsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = cardsState.message,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            is UiState.Success -> {
                val hasSampleCards = cards.any { it.sampleData }
                val totalLimit = cards.sumOf { it.creditLimit }
                val totalBalance = cards.sumOf { it.statementBalance }
                val totalUtilization = if (totalLimit > 0.0) (totalBalance / totalLimit) * 100.0 else 0.0
                val currency = cards.firstOrNull()?.currencySymbol ?: "₹"

                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Worldwide Credit Card Directory Strip
                    item(key = "global_directory_strip") {
                        GlobalDirectoryStrip(
                            catalogState = catalogState,
                            onSelectCountry = onSelectCatalogCountry,
                            onOpenGlobalExplorer = { showGlobalExplorer = true }
                        )
                    }

                    // Portfolio Summary Header
                    if (cards.isNotEmpty()) {
                        item(key = "wallet_summary") {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Credit Portfolio Overview",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        PortfolioStat(
                                            label = "TOTAL BALANCE",
                                            value = formatCurrency(totalBalance, currency)
                                        )
                                        PortfolioStat(
                                            label = "TOTAL LIMIT",
                                            value = formatCurrency(totalLimit, currency)
                                        )
                                        PortfolioStat(
                                            label = "UTILIZATION",
                                            value = formatPercent(totalUtilization)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // One-tap Clear Sample Data Banner
                    if (hasSampleCards) {
                        item(key = "sample_banner") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Sample Demo Cards Active",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "Explore comparisons now or clear sample data in one tap.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                        )
                                    }
                                    TextButton(
                                        onClick = onClearSampleData,
                                        modifier = Modifier.testTag("clear_sample_data_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteSweep,
                                            contentDescription = "Clear sample data",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Clear")
                                    }
                                }
                            }
                        }
                    }

                    if (cards.isEmpty()) {
                        item(key = "empty_cards") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                EmptyStateCard(
                                    icon = Icons.Filled.CreditCard,
                                    title = "Your Wallet Is Empty",
                                    subtitle = "Add your credit cards (last 4 digits only) and configure milestone, fuel, dining, and merchant benefit rules.",
                                    actionLabel = "Add First Credit Card",
                                    onAction = { showAddDialog = true }
                                )
                                OutlinedButton(
                                    onClick = onRestoreSampleData,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("restore_sample_cards_button")
                                ) {
                                    Text("Restore Sample Demo Cards")
                                }
                            }
                        }
                    } else {
                        items(
                            items = cards,
                            key = { it.id }
                        ) { card ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                MetallicCreditCardView(
                                    card = card,
                                    onClick = { selectedCardId = card.id }
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Base ${formatPercent(card.baseRewardRatePercent)} • Bill Day ${card.billingDay}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = { selectedCardId = card.id },
                                        modifier = Modifier.testTag("inspect_card_${card.id}")
                                    ) {
                                        Text("Manage ${card.rules.size} Rules")
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = "Manage card rules",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to add new credit card
        ExtendedFloatingActionButton(
            onClick = {
                prefillTarget = null
                showAddDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_card_fab"),
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add credit card") },
            text = { Text("Add Card") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }

    if (showGlobalExplorer) {
        GlobalCatalogExplorerDialog(
            catalogState = catalogState,
            onSelectCountry = onSelectCatalogCountry,
            onSaveCustomCatalogEntry = onSaveCustomCatalogEntry,
            onPickCardToAdd = { countryCode, bankName, cardName ->
                showGlobalExplorer = false
                prefillTarget = PrefilledCardTarget(
                    countryCode = countryCode,
                    bankName = bankName,
                    cardName = cardName
                )
                showAddDialog = true
            },
            onDismiss = { showGlobalExplorer = false }
        )
    }

    if (showAddDialog) {
        AddOrEditCardDialog(
            existingCard = null,
            catalogState = catalogState,
            prefillCountryCode = prefillTarget?.countryCode,
            prefillBankName = prefillTarget?.bankName,
            prefillCardName = prefillTarget?.cardName,
            onDismiss = {
                showAddDialog = false
                prefillTarget = null
            },
            onSave = { card ->
                onAddCard(card)
                showAddDialog = false
                prefillTarget = null
            }
        )
    }
}

@Composable
private fun PortfolioStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
