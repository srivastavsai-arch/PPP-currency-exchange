package com.ppp.currencyexchange.ui.home

import com.ppp.currencyexchange.data.model.Currency
import com.ppp.currencyexchange.data.model.currencies

data class HomeUiState(
    val selectedTab: Int = 0,
    val amount: String = "",
    val convertedAmount: String = "",
    val fromCurrency: Currency = currencies.first { it.code == "USD" },
    val toCurrency: Currency = currencies.first { it.code == "INR" },
    val isOnline: Boolean = true,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val lastUpdated: String? = null,
    val rates: Map<String, Double> = emptyMap(),
    val manualPairRates: Map<String, Double> = emptyMap(),
    val manualRateInput: String = "",
    val favorites: List<FavoritePair> = emptyList(),
    val isCurrentPairFavorite: Boolean = false,
    val history: List<HistoryEntry> = emptyList(),
    val showCurrencyPicker: Boolean = false,
    val currencyPickerRole: CurrencyPickerRole = CurrencyPickerRole.FROM,
    val currencyPickerSearchQuery: String = "",
    val copiedToClipboard: Boolean = false,
    val inputError: String? = null,
    val pppAmount: String = "",
    val pppCurrency: Currency = currencies.first { it.code == "USD" },
    val pppResult: String = "",
    val pppMarketComparison: String = "",
    val showBigMacCard: Boolean = true
)

data class FavoritePair(
    val fromCurrency: Currency,
    val toCurrency: Currency
)

data class HistoryEntry(
    val fromCurrency: Currency,
    val toCurrency: Currency,
    val fromAmount: Double,
    val toAmount: Double,
    val rate: Double,
    val timestamp: String
)

enum class CurrencyPickerRole {
    FROM, TO
}
