package com.ppp.currencyexchange.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ppp.currencyexchange.data.model.currencies
import com.ppp.currencyexchange.ui.components.AmountInputField
import com.ppp.currencyexchange.ui.components.CurrencyPickerDialog
import com.ppp.currencyexchange.ui.theme.FavoriteGold
import com.ppp.currencyexchange.ui.theme.OfflineOrange
import com.ppp.currencyexchange.ui.theme.OnlineGreen

private val pppCurrencies = currencies.filter { it.code != "INR" }
private val tabTitles = listOf("Convert", "PPP", "Learn")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshRates() }
    )

    if (uiState.showCurrencyPicker) {
        val filterList = if (uiState.selectedTab == 0) currencies
            else pppCurrencies
        CurrencyPickerDialog(
            filterCurrencies = filterList,
            onDismiss = { viewModel.hideCurrencyPicker() },
            onCurrencySelected = { viewModel.selectCurrencyFromPicker(it) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Currency Exchange")
                        Spacer(modifier = Modifier.width(8.dp))
                        OnlineOfflineBadge(isOnline = uiState.isOnline)
                    }
                },
                actions = {
                    Switch(
                        checked = uiState.isOnline,
                        onCheckedChange = { viewModel.toggleOnlineMode() },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = OnlineGreen,
                            uncheckedTrackColor = OfflineOrange,
                            checkedThumbColor = MaterialTheme.colorScheme.surface,
                            uncheckedThumbColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    if (uiState.selectedTab == 0) {
                        IconButton(onClick = { viewModel.refreshRates() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh rates")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                title,
                                fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Crossfade(
                targetState = uiState.selectedTab,
                animationSpec = tween(300)
            ) { tab ->
                when (tab) {
                    0 -> ConvertTab(
                        uiState = uiState,
                        viewModel = viewModel,
                        pullRefreshState = pullRefreshState
                    )
                    1 -> PppTab(uiState = uiState, viewModel = viewModel)
                    2 -> LearnTab()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ConvertTab(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    pullRefreshState: androidx.compose.material.pullrefresh.PullRefreshState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ConversionCard(
                    uiState = uiState,
                    onAmountChange = { viewModel.onAmountChanged(it) },
                    onFromClick = { viewModel.showCurrencyPicker(CurrencyPickerRole.FROM) },
                    onToClick = { viewModel.showCurrencyPicker(CurrencyPickerRole.TO) },
                    onSwap = { viewModel.swapCurrencies() },
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onCopy = { viewModel.copyResult() },
                    onShare = { viewModel.shareResult() },
                    onManualRateChange = { viewModel.onManualRateChanged(it) },
                    onSaveManualRate = { viewModel.saveManualRate() }
                )
            }

            if (uiState.error != null) {
                item {
                    ErrorCard(error = uiState.error!!, onDismiss = { viewModel.refreshRates() })
                }
            }

            item {
                FavoritesSection(
                    favorites = uiState.favorites,
                    onFavoriteClick = { viewModel.useFavoritePair(it) },
                    isCurrentFavorite = uiState.isCurrentPairFavorite
                )
            }

            if (uiState.history.isNotEmpty()) {
                item {
                    HistoryHeader(onClear = { viewModel.clearHistory() }, entryCount = uiState.history.size)
                }
                items(uiState.history) { entry ->
                    HistoryItem(
                        entry = entry,
                        onClick = {
                            viewModel.onFromCurrencyChanged(entry.fromCurrency)
                            viewModel.onToCurrencyChanged(entry.toCurrency)
                            viewModel.onAmountChanged(entry.fromAmount.toString())
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PppTab(uiState: HomeUiState, viewModel: HomeViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PppConverterCard(uiState = uiState, viewModel = viewModel) }
        item { PppExplanationCard() }
        if (uiState.showBigMacCard) {
            item { BigMacCard() }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun PppConverterCard(uiState: HomeUiState, viewModel: HomeViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("PPP Converter", style = MaterialTheme.typography.titleMedium)
            }

            AmountInputField(
                amount = uiState.pppAmount,
                onAmountChange = { viewModel.onPppAmountChanged(it) }
            )

            CurrencySelector(
                label = "From",
                currencyCode = uiState.pppCurrency.code,
                currencyName = uiState.pppCurrency.name,
                onClick = { viewModel.showCurrencyPicker(CurrencyPickerRole.FROM) }
            )

            Text(
                text = "To: INR (PPP)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            HorizontalDivider()

            if (uiState.pppResult.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Market Exchange", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(uiState.pppMarketComparison,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("PPP Adjusted", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(uiState.pppResult,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "PPP reflects purchasing power rather than market exchange rates",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Enter an amount to see PPP-adjusted value",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PppExplanationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("What is PPP?", style = MaterialTheme.typography.titleSmall)
            }
            Text(
                text = "Purchasing Power Parity (PPP) compares currencies by measuring how much a " +
                        "standard basket of goods costs in each country. Unlike market exchange rates " +
                        "which fluctuate daily, PPP provides a stable measure of real purchasing power. " +
                        "These rates use World Bank 2023 data and are updated periodically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Source: World Bank, International Comparison Program (ICP)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun BigMacCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("What is the Big Mac (Hamburger) Rule?",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Text(
                text = "The Big Mac Index, created by The Economist, illustrates PPP using a McDonald's " +
                        "Big Mac burger as a standard basket of goods.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Example:", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Text(
                        text = "\u2022 A Big Mac costs \$5.69 in the USA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "\u2022 The same Big Mac costs \u20B9220 in India",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "\u2022 At market rate: \$5.69 \u00D7 \u20B983 = \u20B9472 (overpriced!)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "\u2022 Big Mac PPP: \u20B9220 / \$5.69 = \u20B938.7 per dollar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Text(
                text = "This shows why market exchange rates alone don't reflect true purchasing power. " +
                        "The official PPP rate from the World Bank is approximately \u20B922.0 per dollar, " +
                        "which accounts for all goods and services, not just hamburgers. " +
                        "This is only an illustrative example; actual calculations use official World Bank PPP factors.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun LearnTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ConceptCard(
            icon = Icons.Default.MonetizationOn,
            title = "Exchange Rate",
            body = "An exchange rate is the price of one currency expressed in terms of another. " +
                    "It is determined by supply and demand in foreign exchange markets and fluctuates " +
                    "constantly. Market exchange rates are influenced by interest rates, inflation, " +
                    "political stability, trade balances, and market speculation.",
            useCase = "Use market exchange rates for actual financial transactions: sending money abroad, " +
                    "traveling, importing/exporting goods, and any situation where currency is " +
                    "physically exchanged."
        ) }
        item { ConceptCard(
            icon = Icons.Default.AccountBalance,
            title = "Purchasing Power Parity (PPP)",
            body = "PPP is an economic theory that compares different currencies using a standardized " +
                    "basket of goods and services. The idea is that identical goods should cost the same " +
                    "in different countries when measured in a common currency (the law of one price). " +
                    "In practice, this doesn't hold perfectly due to transportation costs, taxes, and " +
                    "market inefficiencies.",
            useCase = "Use PPP for comparing economic productivity and living standards between " +
                    "countries. Economists use it to calculate GDP adjusted for purchasing power, " +
                    "which gives a more accurate picture of a country's economic output."
        ) }
        item { ConceptCard(
            icon = Icons.Default.Receipt,
            title = "Cost of Living",
            body = "Cost of living measures the amount of money needed to maintain a certain standard " +
                    "of living in a specific location. It includes housing, food, transportation, " +
                    "healthcare, education, and other expenses. Cost of living indexes compare these " +
                    "costs between cities or countries.",
            useCase = "Use cost of living data when making relocation decisions, negotiating salaries " +
                    "for international positions, or planning retirement in a different country. " +
                    "It helps determine how much income you need to maintain your current lifestyle."
        ) }
        item { ConceptCard(
            icon = Icons.Default.ShoppingCart,
            title = "Purchasing Power",
            body = "Purchasing power refers to the quantity of goods and services that a unit of " +
                    "currency can buy. When prices rise (inflation), purchasing power falls. " +
                    "Similarly, the same amount of money buys different amounts in different countries. " +
                    "For example, $100 has more purchasing power in India than in Switzerland.",
            useCase = "Use purchasing power comparisons to understand the real value of money across " +
                    "different economies. It helps in making informed decisions about where to work, " +
                    "invest, or retire."
        ) }
        item { ConceptCard(
            icon = Icons.Default.TrendingUp,
            title = "When To Use Each",
            body = "Each metric serves a different purpose and answers different questions.",
            useCase = ""
        ) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ComparisonRow("Sending money abroad", "Market rate", "PPP")
                    ComparisonRow("Comparing GDP of countries", "PPP", "Market rate")
                    ComparisonRow("Planning a vacation budget", "Market rate", "PPP")
                    ComparisonRow("Relocating to another country", "Cost of Living", "PPP")
                    ComparisonRow("Long-term economic analysis", "PPP", "Market rate")
                    ComparisonRow("Day-to-day spending abroad", "Market rate", "Cost of Living")
                    ComparisonRow("Salary negotiation (international)", "Cost of Living", "Market rate")
                    ComparisonRow("Investment decisions", "Market rate", "PPP")
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun ConceptCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    useCase: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Text(body, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (useCase.isNotEmpty()) {
                HorizontalDivider()
                Text("When to use:", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
                Text(useCase, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ComparisonRow(scenario: String, best: String, alternative: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(scenario, style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f))
        Text(
            text = best,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "vs",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = alternative,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun OnlineOfflineBadge(isOnline: Boolean) {
    val bgColor = if (isOnline) OnlineGreen else OfflineOrange
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (isOnline) "LIVE" else "MANUAL",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.surface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConversionCard(
    uiState: HomeUiState,
    onAmountChange: (String) -> Unit,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onSwap: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onManualRateChange: (String) -> Unit,
    onSaveManualRate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Convert", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            AmountInputField(amount = uiState.amount, onAmountChange = onAmountChange)

            if (uiState.inputError != null) {
                Text(uiState.inputError!!, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            CurrencySelector(label = "From", currencyCode = uiState.fromCurrency.code,
                currencyName = uiState.fromCurrency.name, onClick = onFromClick)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onSwap, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(onClick = onToggleFavorite) {
                    val icon = if (uiState.isCurrentPairFavorite) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder
                    val tint = if (uiState.isCurrentPairFavorite) FavoriteGold
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    Icon(icon, contentDescription = "Favorite", tint = tint)
                }
            }

            CurrencySelector(label = "To", currencyCode = uiState.toCurrency.code,
                currencyName = uiState.toCurrency.name, onClick = onToClick)

            AnimatedVisibility(
                visible = !uiState.isOnline,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val pairKey = "${uiState.fromCurrency.code}_${uiState.toCurrency.code}"
                    val savedRate = uiState.manualPairRates[pairKey]
                    if (savedRate != null) {
                        Text(
                            text = "Saved rate: 1 ${uiState.fromCurrency.code} = $savedRate ${uiState.toCurrency.code}",
                            style = MaterialTheme.typography.bodySmall, color = OnlineGreen)
                    }
                    OutlinedTextField(
                        value = uiState.manualRateInput,
                        onValueChange = onManualRateChange,
                        label = { Text("Rate (1 ${uiState.fromCurrency.code} = ? ${uiState.toCurrency.code})") },
                        placeholder = if (savedRate != null) {{ Text(savedRate.toString()) }} else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline)
                    )
                    if (uiState.manualRateInput.isNotBlank()) {
                        Button(onClick = onSaveManualRate, modifier = Modifier.fillMaxWidth()) {
                            Text(if (savedRate != null) "Update Rate" else "Save Rate")
                        }
                    }
                }
            }

            HorizontalDivider()

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                AnimatedVisibility(
                    visible = uiState.convertedAmount.isNotBlank(),
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.convertedAmount, style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        Text(uiState.toCurrency.code, style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = onCopy, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null,
                                    modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }
                            OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Share, contentDescription = null,
                                    modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }
                        }
                        if (uiState.copiedToClipboard) {
                            Text("Copied!", style = MaterialTheme.typography.labelSmall, color = OnlineGreen)
                        }
                    }
                }

                if (uiState.amount.isBlank() && !uiState.isLoading) {
                    Text(
                        text = if (uiState.isOnline) "Enter an amount to convert with live rates"
                        else "Enter an amount to convert with manual rates",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isOnline) "Live Rates" else "Manual Rates",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.isOnline) OnlineGreen else OfflineOrange,
                    fontWeight = FontWeight.SemiBold
                )
                uiState.lastUpdated?.let {
                    Text("Updated: $it", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CurrencySelector(label: String, currencyCode: String, currencyName: String, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(currencyCode, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(currencyName, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ErrorCard(error: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(error, color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(onClick = onDismiss) { Text("Try Again") }
        }
    }
}

@Composable
private fun FavoritesSection(
    favorites: List<FavoritePair>,
    onFavoriteClick: (FavoritePair) -> Unit,
    isCurrentFavorite: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = FavoriteGold,
                    modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Favorites", style = MaterialTheme.typography.titleSmall)
                if (isCurrentFavorite) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("(current)", style = MaterialTheme.typography.labelSmall, color = FavoriteGold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (favorites.isEmpty()) {
                Text("Tap the heart icon to save a currency pair as favorite",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(favorites) { pair ->
                        FavoriteChip(pair = pair, onClick = { onFavoriteClick(pair) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteChip(pair: FavoritePair, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = FavoriteGold,
                modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("${pair.fromCurrency.code} \u2192 ${pair.toCurrency.code}",
                style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HistoryHeader(onClear: () -> Unit, entryCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Recent Conversions", style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f))
        Text("$entryCount entries", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        FilledTonalButton(
            onClick = onClear,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer)
        ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun HistoryItem(entry: HistoryEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${entry.fromCurrency.code} \u2192 ${entry.toCurrency.code}",
                    style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${entry.fromAmount} ${entry.fromCurrency.code} = ${entry.toAmount} ${entry.toCurrency.code}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(entry.timestamp, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Rate: ${entry.rate}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}
