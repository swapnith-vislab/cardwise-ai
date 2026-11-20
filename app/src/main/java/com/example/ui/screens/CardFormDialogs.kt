package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BankCardCatalog
import com.example.model.BankCatalogEntry
import com.example.model.CardNetwork
import com.example.model.CardTheme
import com.example.model.CreditCard
import com.example.model.RewardType
import com.example.model.isSafeWebUri
import com.example.model.sanitizeDecimalInput
import com.example.ui.components.PrivacyGuaranteeChip
import com.example.viewmodel.GlobalCatalogUiState

private val CURRENCY_OPTIONS = listOf(
    "₹", "$", "£", "€", "AED", "S$", "CA$", "A$", "¥", "HK$", "RM", "₱", "SAR", "₩", "R$", "MX$", "CHF", "NZ$"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditCardDialog(
    existingCard: CreditCard? = null,
    catalogState: GlobalCatalogUiState = GlobalCatalogUiState(),
    prefillCountryCode: String? = null,
    prefillBankName: String? = null,
    prefillCardName: String? = null,
    onDismiss: () -> Unit,
    onSave: (card: CreditCard) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val isEditing = existingCard != null
    val allBanks = catalogState.allBanks

    val initialBankEntry = remember(existingCard?.issuer, prefillBankName, allBanks) {
        val targetIssuer = existingCard?.issuer ?: prefillBankName
        targetIssuer?.let { BankCardCatalog.findBank(it, allBanks) }
    }
    val initialCardEntry = remember(existingCard?.issuer, existingCard?.cardName, prefillBankName, prefillCardName, allBanks) {
        val targetIssuer = existingCard?.issuer ?: prefillBankName
        val targetCard = existingCard?.cardName ?: prefillCardName
        if (!targetIssuer.isNullOrBlank() && !targetCard.isNullOrBlank()) {
            BankCardCatalog.findCard(targetIssuer, targetCard, allBanks)
        } else {
            null
        }
    }

    var selectedCountryCode by remember {
        mutableStateOf(
            initialBankEntry?.countryCode
                ?: prefillCountryCode
                ?: catalogState.selectedCountryCode.ifBlank { BankCardCatalog.ALL_COUNTRIES_CODE }
        )
    }
    var countryMenuExpanded by remember { mutableStateOf(false) }

    var selectedBankName by remember { mutableStateOf(initialBankEntry?.bankName ?: "") }
    var useCustomBank by remember {
        mutableStateOf(isEditing && initialBankEntry == null && !existingCard?.issuer.isNullOrBlank())
    }
    var customIssuer by remember { mutableStateOf(if (useCustomBank) existingCard?.issuer.orEmpty() else "") }
    var bankMenuExpanded by remember { mutableStateOf(false) }

    var selectedCardName by remember {
        mutableStateOf(initialCardEntry?.cardName ?: prefillCardName.orEmpty())
    }
    var useCustomCard by remember {
        mutableStateOf(isEditing && initialCardEntry == null && !existingCard?.cardName.isNullOrBlank())
    }
    var customCardName by remember { mutableStateOf(if (useCustomCard) existingCard?.cardName.orEmpty() else "") }
    var cardMenuExpanded by remember { mutableStateOf(false) }

    var network by remember {
        mutableStateOf(
            initialCardEntry?.defaultNetwork
                ?: CardNetwork.fromRaw(existingCard?.network ?: CardNetwork.VISA.name)
        )
    }
    var rewardType by remember {
        mutableStateOf(
            initialCardEntry?.defaultRewardType
                ?: RewardType.fromRaw(existingCard?.rewardType ?: RewardType.CASHBACK.name)
        )
    }
    var lastFour by remember { mutableStateOf(existingCard?.lastFour ?: "") }
    var creditLimitText by remember {
        mutableStateOf(
            existingCard?.creditLimit?.takeIf { it > 0 }?.toLong()?.toString() ?: "250000"
        )
    }
    var balanceText by remember {
        mutableStateOf(
            existingCard?.statementBalance?.toLong()?.toString() ?: "0"
        )
    }
    var billingDayText by remember { mutableStateOf((existingCard?.billingDay ?: 15L).toString()) }
    var dueDayText by remember { mutableStateOf((existingCard?.dueDay ?: 5L).toString()) }
    var currencySymbol by remember {
        mutableStateOf(existingCard?.currencySymbol ?: initialBankEntry?.currencySymbol ?: "₹")
    }
    var cardTheme by remember {
        mutableStateOf(
            initialCardEntry?.defaultTheme
                ?: CardTheme.fromRaw(existingCard?.cardTheme ?: CardTheme.EMERALD.name)
        )
    }
    var baseRateText by remember {
        mutableStateOf(
            (existingCard?.baseRewardRatePercent ?: initialCardEntry?.defaultBaseRatePercent ?: 1.0).toString()
        )
    }
    var errorText by remember { mutableStateOf<String?>(null) }

    val banksInCountry = remember(allBanks, selectedCountryCode) {
        if (selectedCountryCode == BankCardCatalog.ALL_COUNTRIES_CODE) {
            allBanks
        } else {
            allBanks.filter { it.countryCode.equals(selectedCountryCode, ignoreCase = true) }
        }
    }
    val selectedBank: BankCatalogEntry? = remember(selectedBankName, allBanks) {
        if (selectedBankName.isBlank()) null else BankCardCatalog.findBank(selectedBankName, allBanks)
    }
    val selectedCountryDisplay = remember(selectedCountryCode, catalogState.countries) {
        catalogState.countries.firstOrNull {
            it.countryCode.equals(selectedCountryCode, ignoreCase = true)
        }?.let {
            if (it.currencySymbol.isNotBlank()) "${it.countryName} (${it.currencySymbol})" else it.countryName
        } ?: BankCardCatalog.ALL_COUNTRIES_NAME
    }

    val availableCardsForBank = selectedBank?.cards ?: emptyList()
    val resolvedIssuerDisplay = when {
        useCustomBank -> BankCardCatalog.CUSTOM_BANK_OPTION
        selectedBank != null -> selectedBank.bankName
        else -> ""
    }
    val resolvedCardDisplay = when {
        useCustomCard -> BankCardCatalog.CUSTOM_CARD_OPTION
        selectedCardName.isNotBlank() -> selectedCardName
        else -> ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Edit Credit Card" else "Add Credit Card")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrivacyGuaranteeChip()

                // 1. Country / Market Dropdown
                ExposedDropdownMenuBox(
                    expanded = countryMenuExpanded,
                    onExpandedChange = { countryMenuExpanded = !countryMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCountryDisplay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("1. Select Country / Market") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Public,
                                contentDescription = "Country market"
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryMenuExpanded)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("country_dropdown_field")
                    )

                    ExposedDropdownMenu(
                        expanded = countryMenuExpanded,
                        onDismissRequest = { countryMenuExpanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
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
                                            text = "${country.bankCount} banks • ${country.cardCount} cards" +
                                                if (country.currencySymbol.isNotBlank()) " • ${country.currencySymbol}" else "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    val changed = selectedCountryCode != country.countryCode
                                    selectedCountryCode = country.countryCode
                                    if (country.currencySymbol.isNotBlank()) {
                                        currencySymbol = country.currencySymbol
                                    }
                                    if (changed && country.countryCode != BankCardCatalog.ALL_COUNTRIES_CODE) {
                                        if (selectedBank != null && !selectedBank.countryCode.equals(country.countryCode, ignoreCase = true)) {
                                            selectedBankName = ""
                                            selectedCardName = ""
                                            useCustomBank = false
                                            useCustomCard = false
                                        }
                                    }
                                    countryMenuExpanded = false
                                },
                                modifier = Modifier.testTag("country_option_${country.countryCode.lowercase()}")
                            )
                        }
                    }
                }

                // 2. Searchable Issuing Bank Selector (supports 70+ Indian & global banks)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = resolvedIssuerDisplay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("2. Select Issuing Bank *") },
                        placeholder = { Text("Tap to search ${banksInCountry.size} banks") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AccountBalance,
                                contentDescription = "Issuing bank"
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Open bank picker"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bank_dropdown_field")
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { bankMenuExpanded = true }
                    )
                }

                if (useCustomBank) {
                    OutlinedTextField(
                        value = customIssuer,
                        onValueChange = { customIssuer = it.take(80) },
                        label = { Text("Enter Custom Bank / Issuer Name *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_issuer_input")
                    )
                }

                // 3. Searchable Credit Card Product Selector (supports 95+ products per bank)
                if (!useCustomBank) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = resolvedCardDisplay,
                            onValueChange = {},
                            readOnly = true,
                            enabled = selectedBank != null,
                            label = { Text("3. Select Credit Card *") },
                            placeholder = {
                                Text(
                                    if (selectedBank != null) {
                                        "Tap to search ${availableCardsForBank.size} ${selectedBank.bankName} cards"
                                    } else {
                                        "Select a bank above first"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CreditCard,
                                    contentDescription = "Credit card variant"
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Open card picker"
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_variant_dropdown_field")
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    if (selectedBank != null) {
                                        cardMenuExpanded = true
                                    } else {
                                        errorText = "Select an issuing bank first to see its credit cards."
                                    }
                                }
                        )
                    }
                }

                if (useCustomCard) {
                    OutlinedTextField(
                        value = customCardName,
                        onValueChange = { customCardName = it.take(100) },
                        label = { Text("Enter Custom Card Variant / Name *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_name_input")
                    )
                }

                // Direct Official Bank Website Link (free browser link)
                val activeBank = selectedBank
                if (activeBank != null && isSafeWebUri(activeBank.officialWebsiteUrl)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    uriHandler.openUri(activeBank.officialWebsiteUrl)
                                } catch (_: Exception) {
                                    // Ignore if no browser available
                                }
                            }
                            .testTag("official_bank_site_chip")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Language,
                                    contentDescription = "Official bank website",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "View benefits on ${activeBank.officialDomain}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.OpenInNew,
                                contentDescription = "Open bank website",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "CARD NETWORK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CardNetwork.entries) { item ->
                        FilterChip(
                            selected = item == network,
                            onClick = { network = item },
                            label = { Text(item.displayName) }
                        )
                    }
                }

                Text(
                    text = "REWARD TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(RewardType.entries) { type ->
                        FilterChip(
                            selected = type == rewardType,
                            onClick = { rewardType = type },
                            label = { Text(type.displayName) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lastFour,
                        onValueChange = { lastFour = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("Last 4 Digits *") },
                        placeholder = { Text("4821") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_last_four_input")
                    )

                    OutlinedTextField(
                        value = baseRateText,
                        onValueChange = { baseRateText = sanitizeDecimalInput(it, maxLength = 6) },
                        label = { Text("Base Rate %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "CURRENCY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CURRENCY_OPTIONS) { sym ->
                        FilterChip(
                            selected = currencySymbol == sym,
                            onClick = { currencySymbol = sym },
                            label = { Text(sym) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = creditLimitText,
                        onValueChange = { creditLimitText = sanitizeDecimalInput(it) },
                        label = { Text("Credit Limit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = sanitizeDecimalInput(it) },
                        label = { Text("Balance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = billingDayText,
                        onValueChange = { billingDayText = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Bill Day (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { dueDayText = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Due Day (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "CARD FINISH",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CardTheme.entries) { theme ->
                        FilterChip(
                            selected = theme == cardTheme,
                            onClick = { cardTheme = theme },
                            label = { Text(theme.displayName) }
                        )
                    }
                }

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanIssuer = if (useCustomBank) {
                        customIssuer.trim()
                    } else {
                        selectedBank?.bankName.orEmpty().trim()
                    }
                    val cleanName = if (useCustomCard) {
                        customCardName.trim()
                    } else {
                        selectedCardName.trim()
                    }
                    val digits = lastFour.filter { it.isDigit() }
                    val limit = creditLimitText.toDoubleOrNull() ?: 0.0
                    val bal = balanceText.toDoubleOrNull() ?: 0.0
                    val billDay = (billingDayText.toLongOrNull() ?: 15L).coerceIn(1L, 31L)
                    val dueDay = (dueDayText.toLongOrNull() ?: 5L).coerceIn(1L, 31L)
                    val baseRate = (baseRateText.toDoubleOrNull() ?: 1.0).coerceIn(0.0, 50.0)

                    when {
                        cleanIssuer.isBlank() -> errorText = "Select an issuing bank from the dropdown (or choose Custom Bank)."
                        cleanName.isBlank() -> errorText = "Select a credit card from the dropdown (or choose Custom Card)."
                        digits.length != 4 -> errorText = "Enter exactly 4 digits for the card ending."
                        else -> {
                            val updatedCard = (existingCard ?: CreditCard()).copy(
                                issuer = cleanIssuer,
                                cardName = cleanName,
                                network = network.name,
                                lastFour = digits,
                                creditLimit = limit.coerceAtLeast(0.0),
                                statementBalance = bal.coerceAtLeast(0.0),
                                billingDay = billDay,
                                dueDay = dueDay,
                                currencySymbol = currencySymbol,
                                cardTheme = cardTheme.name,
                                baseRewardRatePercent = baseRate,
                                rewardType = rewardType.name,
                                aiFetched = false,
                                sampleData = existingCard?.sampleData ?: false
                            )
                            onSave(updatedCard)
                        }
                    }
                },
                modifier = Modifier.testTag("save_card_button")
            ) {
                Text(if (isEditing) "Save Changes" else "Add Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (bankMenuExpanded) {
        BankPickerDialog(
            banks = banksInCountry,
            countryLabel = selectedCountryDisplay,
            onSelectBank = { bank ->
                val changedBank = selectedBank?.bankName != bank.bankName
                selectedBankName = bank.bankName
                useCustomBank = false
                currencySymbol = bank.currencySymbol
                if (changedBank) {
                    selectedCardName = ""
                    useCustomCard = false
                    customCardName = ""
                }
                errorText = null
                bankMenuExpanded = false
            },
            onSelectCustomBank = {
                selectedBankName = ""
                useCustomBank = true
                useCustomCard = true
                selectedCardName = ""
                errorText = null
                bankMenuExpanded = false
            },
            onDismiss = { bankMenuExpanded = false }
        )
    }

    if (cardMenuExpanded && selectedBank != null) {
        CardVariantPickerDialog(
            bankName = selectedBank.bankName,
            cards = availableCardsForBank,
            onSelectCard = { cardItem ->
                selectedCardName = cardItem.cardName
                useCustomCard = false
                network = cardItem.defaultNetwork
                cardTheme = cardItem.defaultTheme
                baseRateText = cardItem.defaultBaseRatePercent.toString()
                rewardType = cardItem.defaultRewardType
                errorText = null
                cardMenuExpanded = false
            },
            onSelectCustomCard = {
                useCustomCard = true
                selectedCardName = ""
                errorText = null
                cardMenuExpanded = false
            },
            onDismiss = { cardMenuExpanded = false }
        )
    }
}
