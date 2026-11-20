package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BankCardCatalog
import com.example.model.CardNetwork
import com.example.model.CreditCardCatalogItem
import com.example.model.RewardType
import com.example.model.formatPercent
import com.example.ui.theme.JetBrainsMono
import com.example.viewmodel.GlobalCatalogUiState
import java.util.Locale

private enum class CatalogQuickFilter(val label: String) {
    ALL("All"),
    RUPAY_UPI("RuPay UPI"),
    CASHBACK("Cashback"),
    TRAVEL_MILES("Travel & Miles"),
    FUEL("Fuel"),
    BUSINESS("Business / Corp"),
    FD_SECURED("FD / Secured")
}

private fun CreditCardCatalogItem.matchesQuickFilter(filter: CatalogQuickFilter): Boolean {
    val lower = cardName.lowercase(Locale.ROOT)
    return when (filter) {
        CatalogQuickFilter.ALL -> true
        CatalogQuickFilter.RUPAY_UPI -> defaultNetwork == CardNetwork.RUPAY || lower.contains("upi") || lower.contains("rupay")
        CatalogQuickFilter.CASHBACK -> defaultRewardType == RewardType.CASHBACK || lower.contains("cashback")
        CatalogQuickFilter.TRAVEL_MILES -> defaultRewardType == RewardType.MILES ||
            lower.contains("travel") || lower.contains("miles") || lower.contains("vistara") ||
            lower.contains("indigo") || lower.contains("air india") || lower.contains("emirates") ||
            lower.contains("ixigo") || lower.contains("scapia") || lower.contains("irctc") ||
            lower.contains("makemytrip") || lower.contains("marriott") || lower.contains("niyo")
        CatalogQuickFilter.FUEL -> lower.contains("fuel") || lower.contains("indianoil") ||
            lower.contains("bpcl") || lower.contains("hpcl") || lower.contains("octane") ||
            lower.contains("energie") || lower.contains("power") || lower.contains("carbon")
        CatalogQuickFilter.BUSINESS -> lower.contains("business") || lower.contains("corporate") ||
            lower.contains("commercial") || lower.contains("biz") || lower.contains("sme") ||
            lower.contains("purchase")
        CatalogQuickFilter.FD_SECURED -> lower.contains("fd") || lower.contains("secured") ||
            lower.contains("wow!") || lower.contains("unnati") || lower.contains("nomo") ||
            lower.contains("step up") || lower.contains("dreamdifferent") || lower.contains("magnet")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalDirectoryStrip(
    catalogState: GlobalCatalogUiState,
    onSelectCountry: (String) -> Unit,
    onOpenGlobalExplorer: () -> Unit
) {
    var countryMenuExpanded by remember { mutableStateOf(false) }
    val activeBanks = catalogState.filteredBanks
    val activeCardCount = remember(activeBanks) { activeBanks.sumOf { it.cards.size } }
    val isIndia = catalogState.selectedCountryCode.equals("IN", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("global_directory_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = "Indian & global card directory",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isIndia) "Indian Credit Card Directory" else "${catalogState.selectedCountryName} Card Directory",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activeBanks.size} banks • $activeCardCount products (${catalogState.totalCardCount} total)",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Active Country Selector
                ExposedDropdownMenuBox(
                    expanded = countryMenuExpanded,
                    onExpandedChange = { countryMenuExpanded = !countryMenuExpanded }
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .clickable { countryMenuExpanded = true }
                            .testTag("directory_country_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (catalogState.selectedCountryCode == BankCardCatalog.ALL_COUNTRIES_CODE) {
                                    "All"
                                } else {
                                    catalogState.selectedCountryCode
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryMenuExpanded)
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = countryMenuExpanded,
                        onDismissRequest = { countryMenuExpanded = false },
                        modifier = Modifier
                            .width(250.dp)
                            .heightIn(max = 320.dp)
                    ) {
                        catalogState.countries.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = country.countryName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                        Text(
                                            text = "${country.bankCount} banks • ${country.cardCount} cards",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectCountry(country.countryCode)
                                    countryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onOpenGlobalExplorer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("explore_world_cards_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isIndia) {
                        "Browse $activeCardCount+ Indian Credit Cards"
                    } else {
                        "Browse Directory (${catalogState.selectedCountryName})"
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalCatalogExplorerDialog(
    catalogState: GlobalCatalogUiState,
    onSelectCountry: (String) -> Unit,
    onSaveCustomCatalogEntry: (
        countryCode: String,
        countryName: String,
        currencySymbol: String,
        bankName: String,
        cardName: String
    ) -> Unit,
    onPickCardToAdd: (countryCode: String, bankName: String, cardName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(CatalogQuickFilter.ALL) }
    var countryDropdownExpanded by remember { mutableStateOf(false) }
    var showAddCustomForm by remember { mutableStateOf(false) }
    var customBankName by remember { mutableStateOf("") }
    var customCardName by remember { mutableStateOf("") }
    var expandedBankKeys by remember { mutableStateOf(setOf("IN_HDFC Bank", "IN_ICICI Bank", "IN_SBI Card")) }

    val matchingEntries = remember(
        catalogState.allBanks,
        catalogState.selectedCountryCode,
        searchQuery,
        activeFilter
    ) {
        val base = catalogState.filteredBanks
        val q = searchQuery.trim().lowercase(Locale.ROOT)
        base.mapNotNull { bank ->
            val bankMatchesQuery = q.isNotBlank() && (
                bank.bankName.lowercase(Locale.ROOT).contains(q) ||
                    bank.countryName.lowercase(Locale.ROOT).contains(q) ||
                    bank.officialDomain.lowercase(Locale.ROOT).contains(q)
                )
            val filteredCards = bank.cards.filter { card ->
                val matchesFilter = card.matchesQuickFilter(activeFilter)
                val matchesText = q.isBlank() || bankMatchesQuery ||
                    card.cardName.lowercase(Locale.ROOT).contains(q) ||
                    card.defaultNetwork.displayName.lowercase(Locale.ROOT).contains(q)
                matchesFilter && matchesText
            }
            if (filteredCards.isNotEmpty()) bank.copy(cards = filteredCards) else null
        }
    }

    val totalMatchingCards = remember(matchingEntries) { matchingEntries.sumOf { it.cards.size } }
    val autoExpandAll = searchQuery.isNotBlank() || activeFilter != CatalogQuickFilter.ALL

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (catalogState.selectedCountryCode.equals("IN", ignoreCase = true)) {
                        "Indian Cards Directory"
                    } else {
                        "Explore Credit Cards"
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Country Filter Dropdown
                ExposedDropdownMenuBox(
                    expanded = countryDropdownExpanded,
                    onExpandedChange = { countryDropdownExpanded = !countryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = catalogState.selectedCountryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Country / Market") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryDropdownExpanded)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("explorer_country_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = countryDropdownExpanded,
                        onDismissRequest = { countryDropdownExpanded = false },
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        catalogState.countries.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Text("${country.countryName} (${country.bankCount} banks, ${country.cardCount} cards)")
                                },
                                onClick = {
                                    onSelectCountry(country.countryCode)
                                    countryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Instant search across 1,100+ Indian & global cards
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Banks, Cards or Networks") },
                    placeholder = { Text("e.g. Infinia, Swiggy, RuPay, BPCL, SBI, PNB…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Filter catalog") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explorer_search_input")
                )

                // Quick Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CatalogQuickFilter.entries) { filter ->
                        FilterChip(
                            selected = activeFilter == filter,
                            onClick = { activeFilter = filter },
                            label = {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                // Live Match Summary
                Text(
                    text = "Showing $totalMatchingCards products across ${matchingEntries.size} banks • Tap a bank to expand",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Free Local Custom Bank/Card Addition
                if (!showAddCustomForm) {
                    OutlinedButton(
                        onClick = { showAddCustomForm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_custom_catalog_form_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Unlisted Bank or Card Locally")
                    }
                } else {
                    val activeCountry = catalogState.countries.firstOrNull {
                        it.countryCode == catalogState.selectedCountryCode
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Add Custom Bank & Card (Saved Locally)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            OutlinedTextField(
                                value = customBankName,
                                onValueChange = { customBankName = it.take(80) },
                                label = { Text("Bank Name *") },
                                placeholder = { Text("e.g. HDFC Bank, Regional Bank") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_catalog_bank_input")
                            )
                            OutlinedTextField(
                                value = customCardName,
                                onValueChange = { customCardName = it.take(100) },
                                label = { Text("Card Variant Name *") },
                                placeholder = { Text("e.g. RuPay Select Metal") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_catalog_card_input")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(onClick = { showAddCustomForm = false }) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = {
                                        val code = catalogState.selectedCountryCode
                                            .takeIf { it != BankCardCatalog.ALL_COUNTRIES_CODE } ?: "IN"
                                        val countryName = activeCountry?.countryName
                                            ?.takeIf { code != BankCardCatalog.ALL_COUNTRIES_CODE } ?: "India"
                                        val sym = activeCountry?.currencySymbol?.ifBlank { "₹" } ?: "₹"
                                        onSaveCustomCatalogEntry(
                                            code,
                                            countryName,
                                            sym,
                                            customBankName.trim(),
                                            customCardName.trim()
                                        )
                                        customBankName = ""
                                        customCardName = ""
                                        showAddCustomForm = false
                                    },
                                    enabled = customBankName.isNotBlank() && customCardName.isNotBlank(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Save to Directory")
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 60fps Flattened LazyColumn: expandable bank headers + individual lazy card items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (matchingEntries.isEmpty()) {
                        item(key = "no_catalog_matches") {
                            Text(
                                text = "No matching cards found. Clear the filter or tap 'Add Unlisted Bank or Card Locally' above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        matchingEntries.forEach { bank ->
                            val bankKey = "${bank.countryCode}_${bank.bankName}"
                            val isExpanded = autoExpandAll || bankKey in expandedBankKeys

                            item(key = "hdr_$bankKey") {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedBankKeys = if (bankKey in expandedBankKeys) {
                                                expandedBankKeys - bankKey
                                            } else {
                                                expandedBankKeys + bankKey
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = bank.bankName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${bank.countryName} (${bank.currencySymbol}) • ${bank.cards.size} cards",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = if (isExpanded) "Collapse ${bank.bankName}" else "Expand ${bank.bankName}",
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }

                            if (isExpanded) {
                                items(
                                    items = bank.cards,
                                    key = { card -> "${bankKey}__${card.cardName}" }
                                ) { card ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp)
                                            .clickable {
                                                onPickCardToAdd(bank.countryCode, bank.bankName, card.cardName)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = card.cardName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${card.defaultNetwork.displayName} • Base ${formatPercent(card.defaultBaseRatePercent)} ${card.defaultRewardType.displayName}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            TextButton(
                                                onClick = {
                                                    onPickCardToAdd(bank.countryCode, bank.bankName, card.cardName)
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = "Add ${card.cardName}",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Add")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
