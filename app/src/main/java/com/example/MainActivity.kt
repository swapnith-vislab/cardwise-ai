package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.CardWiseRepository
import com.example.data.FirestoreProvider
import com.example.data.GlobalCardCatalogRepository
import com.example.data.local.CardWiseLocalDatabase
import com.example.firebase.authStateFlow
import com.example.firebase.signOut
import com.example.ui.screens.ActivitySavingsScreen
import com.example.ui.screens.AdvisorScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MyCardsScreen
import com.example.ui.theme.CardWiseTheme
import com.example.viewmodel.CardWiseViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth

enum class CardWiseTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    ADVISOR("advisor", "Advisor", Icons.Filled.Search, Icons.Outlined.Search),
    MY_CARDS("my_cards", "My Cards", Icons.Filled.CreditCard, Icons.Outlined.CreditCard),
    ACTIVITY("activity", "Activity", Icons.Filled.Insights, Icons.Outlined.Insights)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure FirebaseApp is initialized before accessing Firebase.auth or Firestore
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // Enable Edge-to-Edge layout
        enableEdgeToEdge()

        // Render Auth-gated Jetpack Compose UI
        setContent {
            CardWiseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CardWiseRoot()
                }
            }
        }
    }
}

@Composable
fun CardWiseRoot() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        // Key composition and ViewModel by uid so sign-out or account switch tears down listeners cleanly
        key(user.uid) {
            CardWiseAuthenticatedShell(
                currentUserId = user.uid,
                userDisplayName = user.displayName ?: user.email ?: "CardWise Member",
                userEmail = user.email ?: ""
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardWiseAuthenticatedShell(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String
) {
    val viewModel: CardWiseViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val db = FirestoreProvider.getDatabase(app)
                val localDb = CardWiseLocalDatabase.getInstance(app)
                CardWiseViewModel(
                    repository = CardWiseRepository(db),
                    recentSearchDao = localDb.recentMerchantSearchDao(),
                    catalogRepository = GlobalCardCatalogRepository(dao = localDb.globalCatalogDao()),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val advisorState by viewModel.advisorState.collectAsStateWithLifecycle()
    val cardsState by viewModel.cardsState.collectAsStateWithLifecycle()
    val activitySummary by viewModel.activitySummaryState.collectAsStateWithLifecycle()
    val catalogState by viewModel.catalogState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(CardWiseTab.ADVISOR) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CreditCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showSignOutDialog = true },
                            modifier = Modifier.testTag("account_sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Account and Sign Out"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        CardWiseTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Spacer(modifier = Modifier.size(12.dp))
                        CardWiseTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_rail_${tab.route}")
                            )
                        }
                    }
                }

                when (currentTab) {
                    CardWiseTab.ADVISOR -> AdvisorScreen(
                        state = advisorState,
                        onMerchantChange = viewModel::updateMerchantQuery,
                        onPresetSelected = { merchant, cat, mode ->
                            viewModel.applyQuickMerchantPreset(merchant, cat, mode)
                        },
                        onCategoryChange = viewModel::updateCategory,
                        onAmountChange = viewModel::updateAmountInput,
                        onPaymentModeChange = viewModel::updatePaymentMode,
                        onSelectCardForPayment = viewModel::selectCardForPayment,
                        onNavigateToWallet = { currentTab = CardWiseTab.MY_CARDS },
                        modifier = Modifier.weight(1f)
                    )

                    CardWiseTab.MY_CARDS -> MyCardsScreen(
                        cardsState = cardsState,
                        catalogState = catalogState,
                        onAddCard = viewModel::addCard,
                        onUpdateCard = viewModel::updateCard,
                        onDeleteCard = viewModel::deleteCard,
                        onSaveRule = viewModel::saveRuleOverride,
                        onDeleteRule = viewModel::deleteRuleFromCard,
                        onClearSampleData = viewModel::clearSampleData,
                        onRestoreSampleData = viewModel::restoreSampleData,
                        onSelectCatalogCountry = viewModel::selectCatalogCountry,
                        onSaveCustomCatalogEntry = { countryCode, countryName, currencySymbol, bankName, cardName ->
                            viewModel.saveCustomBankAndCardToCatalog(
                                countryCode = countryCode,
                                countryName = countryName,
                                currencySymbol = currencySymbol,
                                bankName = bankName,
                                cardName = cardName
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CardWiseTab.ACTIVITY -> ActivitySavingsScreen(
                        summary = activitySummary,
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onNavigateToAdvisor = { currentTab = CardWiseTab.ADVISOR },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Signed In Account") },
            text = {
                Column {
                    Text(text = userDisplayName, style = MaterialTheme.typography.titleMedium)
                    if (userEmail.isNotBlank()) {
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "Your credit cards and savings activity sync securely with Cloud Firestore and stay cached offline on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        signOut(credentialManager = credentialManager, scope = scope)
                    },
                    modifier = Modifier.testTag("confirm_sign_out_button")
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
